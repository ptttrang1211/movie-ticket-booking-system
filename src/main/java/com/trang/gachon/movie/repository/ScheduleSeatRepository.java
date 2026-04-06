package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.ScheduleSeat;
import com.trang.gachon.movie.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat,Long> {
    // Lấy tất cả trạng thái ghế của 1 suất chiếu → render sơ đồ
    List<ScheduleSeat> findByShowtime_ShowTimeId(Long showtimeId);

    // Tìm trạng thái 1 ghế cụ thể trong 1 suất chiếu
    Optional<ScheduleSeat> findBySeat_SeatIdAndShowtime_ShowTimeId(
            Long seatId, Long showtimeId
    );


    /// Đổi trạng thái nhiều ghế cùng lúc khi confirm
    @Modifying
    @Query("UPDATE ScheduleSeat ss SET ss.seatStatus = :status " +
            "WHERE ss.seat.seatId IN :seatIds " +
            "AND ss.showtime.showTimeId = :showTimeId")
    void updateStatusBySeatIds(
            @Param("seatIds")    List<Long>  seatIds,
            @Param("showtimeId") Long        showTimeId,
            @Param("seatStatus")     SeatStatus  seatStatus
    );



    /**
     * Kiểm tra có ghế nào đã bán (SOLD) trong suất chiếu này không
     */
    boolean existsByShowtime_ShowTimeIdAndSeatStatus(Long showTimeId, SeatStatus seatStatus);
}