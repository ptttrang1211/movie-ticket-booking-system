package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.ShowTimeRequest;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.enums.SeatStatus;
import com.trang.gachon.movie.exception.*;
import com.trang.gachon.movie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowTimeService {

    private final ShowTimeRepository showTimeRepository;
    private final MovieRepository movieRepository;
    private final CinemaRoomRepository cinemaRoomRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    // ==================== PUBLIC - LẤY DANH SÁCH ====================

    /**
     * Lấy suất chiếu theo phim + ngày
     */
    public List<Showtime> getShowTimesByMovieAndDate(Long movieId, LocalDate showDate) {
        return showTimeRepository.findByMovie_MovieIdAndShowDateOrderByShowTimeAsc(movieId, showDate);
    }

    /**
     * Lấy suất chiếu theo phim + chi nhánh + ngày
     */
    public List<Showtime> getShowTimesByMovieAndBranchAndDate(Long movieId, Long branchId, LocalDate showDate) {
        return showTimeRepository.findByMovie_MovieIdAndCinemaRoom_Branch_BranchIdAndShowDateOrderByShowTimeAsc(movieId, branchId, showDate);
    }

    /**
     * Lấy suất chiếu theo chi nhánh + ngày
     */
    public List<Showtime> getShowTimesByBranchAndDate(Long branchId, LocalDate showDate) {
        return showTimeRepository.findByCinemaRoom_Branch_BranchIdAndShowDateOrderByShowTimeAsc(branchId, showDate);
    }



    /**
     * Lấy all suất chiếu trong ngày
     */
    public List<Showtime> getShowTimesByDate(LocalDate showDate) {
        return showTimeRepository.findByShowDateOrderByShowTimeAsc(showDate);
    }

    /**
     * Lấy suất chiếu theo ID
     */
    public Showtime getShowTimeById(Long showTimeId) {
        return showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new ShowTimeException("Không tìm thấy suất chiếu với id: " + showTimeId, "NOT_FOUND"));
    }

    /**
     * Lấy tất cả suất chiếu (admin)
     */
    public List<Showtime> getAllShowTimes() {
        return showTimeRepository.findAll();
    }

    // ==================== ADMIN - THÊM, SỬA, XÓA ====================

    /**
     * Thêm suất chiếu mới
     */
    @Transactional
    public void addShowTime(ShowTimeRequest showTimeRequest) {
        // Validate input (Spring đã validate rồi, nhưng có thể kiểm tra thêm business logic)
        Movie movie = findMovieOrThrow(showTimeRequest.getMovieId());
        CinemaRoom cinemaRoom = findCinemaRoomOrThrow(showTimeRequest.getCinemaRoomId());

        // Kiểm tra xung đột lịch
        if (isShowTimeConflict(
                showTimeRequest.getCinemaRoomId(),
                showTimeRequest.getShowDate(),
                showTimeRequest.getShowTime(),
                null
        )) {
            throw new ShowTimeException(
                    "Giờ chiếu này đã tồn tại trong cùng phòng! (" + showTimeRequest.getShowTime() + ")",
                    "CONFLICT"
            );
        }

        // Lưu suất chiếu
        Showtime showtime = Showtime.builder()
                .movie(movie)
                .cinemaRoom(cinemaRoom)
                .showDate(showTimeRequest.getShowDate())
                .showTime(showTimeRequest.getShowTime())
                .build();

        showTimeRepository.save(showtime);
    }
    @Transactional
    public void updateShowTime(Long showTimeId, ShowTimeRequest showTimeRequest) {
        Showtime showtime = getShowTimeById(showTimeId);

        Movie movie = findMovieOrThrow(showTimeRequest.getMovieId());
        CinemaRoom cinemaRoom = findCinemaRoomOrThrow(showTimeRequest.getCinemaRoomId());

        // Kiểm tra xung đột (trừ chính suất này)
        if (isShowTimeConflict(
                showTimeRequest.getCinemaRoomId(),
                showTimeRequest.getShowDate(),
                showTimeRequest.getShowTime(),
                showTimeId
        )) {
            throw new ShowTimeException(
                    "Giờ chiếu này đã tồn tại trong cùng phòng! (" + showTimeRequest.getShowTime() + ")",
                    "CONFLICT"
            );
        }

        // Cập nhật
        showtime.setMovie(movie);
        showtime.setCinemaRoom(cinemaRoom);
        showtime.setShowDate(showTimeRequest.getShowDate());
        showtime.setShowTime(showTimeRequest.getShowTime());

        showTimeRepository.save(showtime);
    }

    /**
     * Xóa suất chiếu
     */
    @Transactional
    public void deleteShowTime(Long showTimeId) {
        Showtime showtime = getShowTimeById(showTimeId);

        // Kiểm tra có vé bán không
        if (hasAnySoldSeat(showTimeId)) {
            throw new ShowTimeException(
                    "Không thể xóa suất chiếu này vì đã có vé bán ra!",
                    "DELETION_ERROR"
            );
        }

        showTimeRepository.deleteById(showTimeId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Kiểm tra xung đột lịch chiếu (chỉ check giờ trùng, DB có UNIQUE constraint)
     * @param cinemaRoomId ID phòng chiếu
     * @param showDate Ngày chiếu
     * @param showTime Giờ chiếu
     * @param excludeShowTimeId ID suất chiếu cần loại trừ (dùng khi sửa)
     * @return true nếu tồn tại xung đột, false nếu an toàn
     */
    private boolean isShowTimeConflict(Long cinemaRoomId, LocalDate showDate, LocalTime showTime, Long excludeShowTimeId) {
        List<Showtime> sameRoomShowtimes =
                showTimeRepository.findByCinemaRoom_CinemaRoomIdAndShowDate(
                        cinemaRoomId, showDate
                );

        return sameRoomShowtimes.stream().anyMatch(existing ->
                existing.getShowTime() != null
                        && existing.getShowTime().equals(showTime)
                        && (excludeShowTimeId == null
                        || !existing.getShowTimeId().equals(excludeShowTimeId))
        );
    }

    /**
     * Kiểm tra có ghế nào đã bán trong suất chiếu này
     */
    private boolean hasAnySoldSeat(Long showTimeId) {
        return scheduleSeatRepository.existsByShowtime_ShowTimeIdAndSeatStatus(showTimeId, SeatStatus.SOLD);
    }

    /**
     * Lấy phim hoặc throw exception
     */
    private Movie findMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ShowTimeException(
                        "Không tìm thấy phim với id: " + movieId,
                        "NOT_FOUND"
                ));
    }

    /**
     * Lấy phòng chiếu hoặc throw exception
     */
    private CinemaRoom findCinemaRoomOrThrow(Long cinemaRoomId) {
        return cinemaRoomRepository.findById(cinemaRoomId)
                .orElseThrow(() -> new ShowTimeException(
                        "Không tìm thấy phòng chiếu với id: " + cinemaRoomId,
                        "NOT_FOUND"
                ));
    }

    /**
     * Map entity → request (để bind vào form edit)
     */
    public ShowTimeRequest toRequest(Showtime showtime) {
        if (showtime == null) {
            throw new ShowTimeException("Showtime không được null!", "VALIDATION_ERROR");
        }

        return ShowTimeRequest.builder()
                .movieId(showtime.getMovie().getMovieId())
                .cinemaRoomId(showtime.getCinemaRoom().getCinemaRoomId())
                .showDate(showtime.getShowDate())
                .showTime(showtime.getShowTime())
                .build();
    }
}
