package com.trang.gachon.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MovieRequest {

    @NotBlank(message = "Tên phim (tiếng Anh) không được để trống")
    private String movieNameEnglish;

    private String movieNameVn;
    private String actor;
    private String director;
    private String movieProductionCompany;

    @NotNull(message = "Thời lượng không được để trống")
    @jakarta.validation.constraints.Positive(message = "Thời lượng phải lớn hơn 0")
    private Integer duration;

    // "2D" | "3D" | "2D, 3D"
    private String version;

    @NotNull(message = "Ngày bắt đầu chiếu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

    private String trailer;
    private String reviewLink;
    private String content;
    private String bannerImageUrl;
    private boolean featuredOnHome;

    @NotEmpty(message = "Phim phải có ít nhất 1 thể loại")
    private List<Long> typeIds;
}
