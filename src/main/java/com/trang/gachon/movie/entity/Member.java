package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "member_code")
    private String memberCode;

    //điểm tích luỹ mặc định bằng 0
    @Column(name = "score")
    private Long score = 0L;

    // FK -> account(1:1) : 1 member - 1 account
    @OneToOne(fetch = FetchType.LAZY) // 1 member - 1 account
    @JoinColumn(name = "account_id", nullable = false,foreignKey = @ForeignKey(name = "fk_member_account_id" ) )
    private Account  account;
    

}
