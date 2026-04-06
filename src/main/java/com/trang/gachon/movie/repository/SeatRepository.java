package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Seat;
import com.trang.gachon.movie.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat,Long> {

    //lấy tất cả các ghế trong phòng, sắp xếp để hiển thị sơ đồ
    List<Seat>findByCinemaRoom_CinemaRoomIdOrderBySeatRowAscSeatColumnAsc(Long id);

    //lấy ghế theo loại trong phòng
    List<Seat> findByCinemaRoom_CinemaRoomIdAndSeatType (Long cinemaRoomId, SeatType seatType);
}
