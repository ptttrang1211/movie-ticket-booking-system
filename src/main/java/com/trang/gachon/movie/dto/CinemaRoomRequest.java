package com.trang.gachon.movie.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CinemaRoomRequest {

    @NotBlank(message = "Tên phòng không được để trống")
    private String cinemaRoomName;

    @NotNull(message = "Vui lòng chọn chi nhánh")
    private Long branchId;

    // Giới hạn vừa phải để layout ghế không quá phi thực tế và UI vẫn dễ quản lý.
    @NotNull(message = "Số lượng ghế không được để trống")
    @Min(value = 10, message = "Phòng chiếu cần ít nhất 10 ghế")
    @Max(value = 200, message = "Số lượng ghế tối đa là 200")
    private Integer seatQuantity;
}
