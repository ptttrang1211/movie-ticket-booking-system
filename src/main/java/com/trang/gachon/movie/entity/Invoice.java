package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long invoiceId;

    // Mã booking hiển thị UI: "SV7FuDAkwX"
    @Column(name = "booking_code", nullable = false, unique = true, length = 20)
    private String bookingCode;

    //snapshot tại thời điểm đặt
    //lưu string thay vì fk vì nếu sửa tên phim, hoá đơn cũ vẫn đúng
    @Column(name = "movie_name",  length = 255)
    private String movieName;

    @Column(name = "cinema_room", length = 255)
    private String cinemaRoom;

    @Column(name = "schedule_show")
    private LocalDate scheduleShow;

    // "21:00"
    @Column(name = "schedule_show_time",length = 20)
    private String scheduleShowTime;

    //1D 1E 1F - danh sách ghế cách nhau bằng dấu cách
    @Column(name = "seat", length = 255)
    private String seat;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    //data lưu string "waiting"|"confirmed"| "cancelled"
    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 20)
    private InvoiceStatus invoiceStatus = InvoiceStatus.WAITING;

    @Column(name = "total_money")
    private Long totalMoney;

    //điểm được cộng sau khi thanh toán
    @Column(name = "add_score")
    private Long addScore = 0L;

    //điểm đã dùng để quy đổi vé
    @Column(name = "user_score")
    private Long useScore = 0L;

    //FK -> account(N:1) : 1 account có nhiều invoice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false,foreignKey = @ForeignKey(name = "fk_invoice_account_id"))
    private Account account;

}
