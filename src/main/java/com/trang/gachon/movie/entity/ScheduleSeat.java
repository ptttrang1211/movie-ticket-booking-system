package com.trang.gachon.movie.entity;


import com.trang.gachon.movie.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "schedule_seat")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column( name = "schedule_seat_id")
    private Long scheduleSeatId;

    //db lưu string available, selecting,sold
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status", nullable = false, length = 20)
    private SeatStatus seatStatus  = SeatStatus.AVAILABLE;

    //ngày chiếu cụ thể - 1 phim chiếu nhiều ngày
    @Column(name =  "show_date", nullable = false)
    private LocalDate showDate;

    //fk -> seat(N :1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ss_seat_id" ))
    private Seat  seat;

    //fk -> schedule : 1 schedule có nhiều scheduleSeat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ss_schedule_id"))
    private Schedule schedule;

    //fk -> movie (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ss_movie_id"))
    private Movie  movie;
    
    
}
