package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoice")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    // Mã booking hiển thị UI: "SV7FuDAkwX"
    @Column(name = "booking_code", nullable = false, unique = true, length = 20)
    private String bookingCode;

    //snapshot tại thời điểm đặt
    //lưu string thay vì fk vì nếu sửa tên phim, hoá đơn cũ vẫn đúng
    @Column(name = "movie_name", columnDefinition = "NVARCHAR(255)")
    private String movieName;

    @Column(name = "cinema_room", columnDefinition = "NVARCHAR(255)")
    private String cinemaRoom;

    @Column(name = "schedule_show")
    private LocalDate scheduleShow;

    // "21:00"
    @Column(name = "schedule_show_time", columnDefinition = "NVARCHAR(20)")
    private String scheduleShowTime;

    //1D 1E 1F - danh sách ghế cách nhau bằng dấu cách
    @Column(name = "seat", columnDefinition = "NVARCHAR(255)")
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
    //Cho phép null để employee có thể bán vé cho khách vãng lai tại quầy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = true,foreignKey = @ForeignKey(name = "fk_invoice_account_id"))
    private Account account;

    // Nhân viên/quản trị viên nào là người xử lý invoice này.
    // Mình lưu Account thay vì Employee riêng để bám đúng design hiện tại:
    // employee chỉ là phần mở rộng 1-1 của account.
    // Nhờ vậy cả flow "bán tại quầy" lẫn "xác nhận booking online" đều dùng chung được.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "handled_by_account_id",
            nullable = true,
            foreignKey = @ForeignKey(name = "fk_invoice_handled_by_account_id")
    )
    private Account handledBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @ManyToOne(optional = true) //nhiều invoice thì có 1 promotion, or ko có 
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

}
