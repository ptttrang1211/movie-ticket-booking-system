package com.trang.gachon.movie.dto;

import com.trang.gachon.movie.enums.BranchStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BranchRequest {

    @NotBlank(message = "Tên chi nhánh không được để trống")
    @Size(max = 255, message = "Tên chi nhánh tối đa 255 ký tự")
    private String branchName;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự")
    private String image;

    @Size(max = 1000, message = "Map URL tối đa 1000 ký tự")
    private String mapEmbedUrl;

    private BranchStatus branchStatus = BranchStatus.ACTIVE;
}
