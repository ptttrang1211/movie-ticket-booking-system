package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.AccountStatus;
import com.trang.gachon.movie.converter.AccountStatusConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_username",      columnNames = "username"),
                @UniqueConstraint(name = "uk_account_email",         columnNames = "email"),
                @UniqueConstraint(name = "uk_account_identity_card", columnNames = "identity_card"),
                @UniqueConstraint(name = "uk_account_phone_number",  columnNames = "phone_number")
        },
        indexes = {
                @Index(name = "idx_account_role_id", columnList = "role_id"),
                @Index(name = "idx_account_status",  columnList = "account_status")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "username", nullable = false, length = 50 )
    private String userName;

    @Column(name = "password", nullable = false, length = 100 )
    private String password;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String  fullName;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    //todo: fomat sdt
    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "identity_card", length = 50)
    private String identityCard;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    //nam or nữ
    @Column(name = "gender", columnDefinition = "NVARCHAR(20)")
    private String gender;

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "register_date")
    private LocalDate registerDate;

    @Column(name = "pending_created_at")
    private LocalDateTime pendingCreatedAt;

    //DB lưu int 1/2 -> converter map sang accountStatus enum
    @Convert(converter = AccountStatusConverter.class)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    //FK -> roles (N:1)
    //FetchType.EAGER vì security cần load role ngay khi login
    @ManyToOne(fetch = FetchType.EAGER)    //nhiều account thuộc về 1 role
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_account_role_id"))
    private Role  role;

    //quan hệ ngược ko tạo cột
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Member   member;

    @OneToOne(mappedBy = "account" , cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employee  employee;
}

//bắt exception của ngày tháng
//
