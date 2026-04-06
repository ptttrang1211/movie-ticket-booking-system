package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "employee")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long EmployeeId;

    //FK -> account (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @ForeignKey(name = "fk_employee_account_id") )
    private Account  account;
}
