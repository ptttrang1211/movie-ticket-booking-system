package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.CinemaRoomRequest;
import com.trang.gachon.movie.entity.Branch;
import com.trang.gachon.movie.entity.CinemaRoom;
import com.trang.gachon.movie.entity.Seat;
import com.trang.gachon.movie.enums.SeatType;
import com.trang.gachon.movie.repository.BranchRepository;
import com.trang.gachon.movie.repository.CinemaRoomRepository;
import com.trang.gachon.movie.repository.SeatRepository;
import com.trang.gachon.movie.repository.ShowTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaRoomService {
    private final CinemaRoomRepository cinemaRoomRepository;
    private final SeatRepository seatRepository;
    private final BranchRepository branchRepository;
    private final ShowTimeRepository showTimeRepository;

    public List<CinemaRoom> getAll() {
        return cinemaRoomRepository.findAll().stream()
                .sorted(Comparator
                        .comparing((CinemaRoom room) -> room.getBranch().getBranchName())
                        .thenComparing(CinemaRoom::getCinemaRoomName))
                .toList();
    }

    public List<CinemaRoom> getByBranch(Long branchId) {
        return cinemaRoomRepository.findByBranch_BranchIdOrderByCinemaRoomNameAsc(branchId);
    }

    public CinemaRoom getById(Long id) {
        return cinemaRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chiếu!"));
    }

    public List<Seat> getSeatsByRoom(Long cinemaRoomId) {
        return seatRepository
                .findByCinemaRoom_CinemaRoomIdOrderBySeatRowAscSeatColumnAsc(cinemaRoomId);
    }

    @Transactional
    public void addRoom(CinemaRoomRequest req) {
        Branch branch = findBranchOrThrow(req.getBranchId());
        validateUniqueRoomName(req.getCinemaRoomName(), branch.getBranchId(), null);

        // Tạo phòng trước để có roomId, sau đó mới sinh toàn bộ ghế theo layout mặc định.
        CinemaRoom room = cinemaRoomRepository.save(CinemaRoom.builder()
                .cinemaRoomName(req.getCinemaRoomName().trim())
                .seatQuantity(req.getSeatQuantity())
                .branch(branch)
                .build());

        seatRepository.saveAll(buildSeatsForRoom(room, req.getSeatQuantity()));
    }

    @Transactional
    public void updateRoom(Long id, CinemaRoomRequest req) {
        CinemaRoom room = getById(id);
        Branch targetBranch = findBranchOrThrow(req.getBranchId());

        validateUniqueRoomName(req.getCinemaRoomName(), targetBranch.getBranchId(), room.getCinemaRoomId());

        boolean hasShowtimes = showTimeRepository.existsByCinemaRoom_CinemaRoomId(room.getCinemaRoomId());
        boolean branchChanged = !room.getBranch().getBranchId().equals(targetBranch.getBranchId());
        boolean seatQtyChanged = !room.getSeatQuantity().equals(req.getSeatQuantity());

        // Nếu phòng đã có showtime thì không cho đổi branch/seatQuantity,
        // vì 2 thay đổi này sẽ làm lệch layout ghế và các suất chiếu đang dùng.
        if (hasShowtimes && (branchChanged || seatQtyChanged)) {
            throw new IllegalArgumentException(
                    "Phòng đã có lịch chiếu nên không thể đổi chi nhánh hoặc số lượng ghế!"
            );
        }

        room.setCinemaRoomName(req.getCinemaRoomName().trim());
        room.setBranch(targetBranch);

        if (seatQtyChanged) {
            room.setSeatQuantity(req.getSeatQuantity());
            cinemaRoomRepository.save(room);

            // Chỉ rebuild seat map khi phòng chưa từng được dùng cho showtime.
            seatRepository.deleteAll(getSeatsByRoom(room.getCinemaRoomId()));
            seatRepository.saveAll(buildSeatsForRoom(room, req.getSeatQuantity()));
            return;
        }

        cinemaRoomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        CinemaRoom room = getById(id);

        // Không xóa cứng phòng đã có lịch chiếu để tránh làm gãy showtime/booking cũ.
        if (showTimeRepository.existsByCinemaRoom_CinemaRoomId(room.getCinemaRoomId())) {
            throw new IllegalArgumentException("Phòng đã có lịch chiếu, không thể xóa!");
        }

        seatRepository.deleteAll(getSeatsByRoom(room.getCinemaRoomId()));
        cinemaRoomRepository.delete(room);
    }

    @Transactional
    public void updateSeatTypes(Long cinemaRoomId, List<Long> vipSeatIds) {
        List<Seat> seats = getSeatsByRoom(cinemaRoomId);
        List<Long> safeVipSeatIds = vipSeatIds == null ? List.of() : vipSeatIds;

        seats.forEach(seat -> {
            SeatType newType = safeVipSeatIds.contains(seat.getSeatId())
                    ? SeatType.VIP
                    : SeatType.NORMAL;
            seat.setSeatType(newType);
        });

        seatRepository.saveAll(seats);
    }

    public CinemaRoomRequest toRequest(CinemaRoom room) {
        return new CinemaRoomRequest(
                room.getCinemaRoomName(),
                room.getBranch().getBranchId(),
                room.getSeatQuantity()
        );
    }

    private Branch findBranchOrThrow(Long branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh!"));
    }

    private void validateUniqueRoomName(String roomName, Long branchId, Long currentRoomId) {
        String normalizedName = roomName == null ? "" : roomName.trim();

        boolean exists = currentRoomId == null
                ? cinemaRoomRepository.existsByBranch_BranchIdAndCinemaRoomNameIgnoreCase(branchId, normalizedName)
                : cinemaRoomRepository.existsByBranch_BranchIdAndCinemaRoomNameIgnoreCaseAndCinemaRoomIdNot(
                        branchId, normalizedName, currentRoomId
                );

        if (exists) {
            throw new IllegalArgumentException("Tên phòng đã tồn tại trong chi nhánh này!");
        }
    }

    private List<Seat> buildSeatsForRoom(CinemaRoom room, int seatQuantity) {
        int seatsPerRow = 10;
        int totalRows = (int) Math.ceil((double) seatQuantity / seatsPerRow);

        // Logic nhẹ nhàng một chút cho rạp:
        // - Phòng nhỏ: chỉ hàng cuối là VIP
        // - Phòng từ 3 hàng trở lên: 2 hàng cuối là VIP
        int vipStartRowIndex = totalRows <= 2 ? totalRows - 1 : totalRows - 2;

        java.util.ArrayList<Seat> seats = new java.util.ArrayList<>();

        for (int index = 0; index < seatQuantity; index++) {
            int rowIndex = index / seatsPerRow;
            int column = (index % seatsPerRow) + 1;

            seats.add(Seat.builder()
                    .seatRow(toSeatRowLabel(rowIndex))
                    .seatColumn(column)
                    .seatType(rowIndex >= vipStartRowIndex ? SeatType.VIP : SeatType.NORMAL)
                    .cinemaRoom(room)
                    .build());
        }

        return seats;
    }

    private String toSeatRowLabel(int rowIndex) {
        // 0 -> A, 25 -> Z, 26 -> AA ...
        StringBuilder label = new StringBuilder();
        int value = rowIndex;

        do {
            label.insert(0, (char) ('A' + (value % 26)));
            value = (value / 26) - 1;
        } while (value >= 0);

        return label.toString();
    }
}
