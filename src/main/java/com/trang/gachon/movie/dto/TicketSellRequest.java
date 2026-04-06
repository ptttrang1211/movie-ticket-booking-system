package com.trang.gachon.movie.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TicketSellRequest {

    private Long showtimeId;
    private List<Long> seatIds;

    // Employee nhập memberCode hoặc identityCard để tìm member
    private String memberKeyword;

    // Dùng điểm quy đổi không
    private boolean useScore = false;

    // Promotion muốn áp dụng (nullable)
    private Long promotionId;
}
