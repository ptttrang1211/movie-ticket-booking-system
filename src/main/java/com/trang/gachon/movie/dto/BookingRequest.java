package com.trang.gachon.movie.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class BookingRequest {

    // Showtime đã chứa: movie + cinemaRoom + showDate + showTime
    private Long showtimeId;

    // Danh sách seat_id đã chọn
    private List<Long> seatIds;

    // Dùng điểm quy đổi không
    private boolean useScore = false;

    // Promotion muốn áp dụng (nullable — có thể không chọn)
    private Long promotionId;
}
