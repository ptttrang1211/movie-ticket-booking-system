package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotion")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "detail", columnDefinition = "NVARCHAR(MAX)")
    private String detail;

    //mức giảm giá (vnd)
    @Column(name = "discount_level")
    private Long discountLevel;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "image", columnDefinition = "NVARCHAR(1000)")
    private String image;

    @OneToMany(mappedBy = "promotion")
    private List<Invoice> invoices;


    // Helper method kiểm tra còn hiệu lực không
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        if (startTime != null && now.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && now.isAfter(endTime)) {
            return false;
        }
        return true;
    }
}
