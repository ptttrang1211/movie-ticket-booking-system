package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Repository
public interface ShowTimeRepository extends JpaRepository<Showtime, Long> {

        // Lấy tất cả suất chiếu của 1 phim theo ngày
        List<Showtime> findByMovie_MovieIdAndShowDateOrderByShowTimeAsc(
            Long movieId, LocalDate showDate
    );

        // Lấy tất cả suất chiếu trong ngày (trang showtimes)
        List<Showtime> findByShowDateOrderByShowTimeAsc(LocalDate showDate);

        // Lấy suất chiếu theo phòng + ngày (kiểm tra trùng khi tạo mới)
        List<Showtime> findByCinemaRoom_CinemaRoomIdAndShowDate(
            Long cinemaRoomId, LocalDate showDate
    );

        // Lấy suất chiếu theo chi nhánh + ngày (trang chọn chi nhánh)
        List<Showtime> findByCinemaRoom_Branch_BranchIdAndShowDateOrderByShowTimeAsc(
            Long branchId, LocalDate showDate
    );
        //Lấy suất chiếu theo phim + chi nhánh + ngày (trang showtimes)
        List<Showtime> findByMovie_MovieIdAndCinemaRoom_Branch_BranchIdAndShowDateOrderByShowTimeAsc(
            Long movieId, Long branchId, LocalDate showDate
        );

    /**
     * Kiểm tra xung đột lịch chiếu
     * Trả về true nếu tồn tại suất chiếu cùng phòng, cùng ngày, cùng giờ (trừ excludeShowTimeId)
     */
    boolean existsByCinemaRoom_CinemaRoomIdAndShowDateAndShowTimeAndShowTimeIdNot(
            Long cinemaRoomId, LocalDate showDate, LocalTime showTime, Long excludeShowTimeId
    );

    boolean existsByCinemaRoom_CinemaRoomId(Long cinemaRoomId);


}

