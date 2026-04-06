package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "schedule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_schedule_time",
                columnNames = "schedule_time"
        )
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    //"08:00" | "09:00"....
    @Column(name = "schedule_time", nullable = false,  length = 20)
    private String scheduleTime;
}
