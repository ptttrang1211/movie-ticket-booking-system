package com.trang.gachon.movie.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShowTimeRequest {
    @NotNull(message = "phim không được để trống")
    private Long movieId;

    @NotNull(message = "phòng chiếu không được để trống")
    private Long cinemaRoomId;

    @NotNull(message = "ngày chiếu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate showDate;

    @NotNull(message = "giờ chiếu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime showTime;

}
