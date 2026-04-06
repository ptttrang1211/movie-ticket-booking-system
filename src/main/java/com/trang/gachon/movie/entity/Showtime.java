package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
// SHOWTIME: suất chiếu cụ thể
// = 1 phim + 1 phòng chiếu + 1 ngày + 1 giờ
//
// Constraint UNIQUE (cinema_room_id, show_date, show_time)
// → 1 phòng KHÔNG thể chiếu 2 phim cùng giờ cùng ngày

@Entity
@Table(name = "showtime",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_showtime_room_date_time",
                columnNames = {"cinema_room_id", "show_date", "show_time"}
        ),
        indexes = {
                @Index(name = "idx_showtime_movie_id",       columnList = "movie_id"),
                @Index(name = "idx_showtime_cinema_room_id", columnList = "cinema_room_id"),
                @Index(name = "idx_showtime_show_date",      columnList = "show_date")}
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "showtime_id")
    private Long showTimeId;

    // Ngày chiếu: 2025-01-12
    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    // Giờ chiếu: "08:00" | "14:30" | "21:00"
    @Column(name = "show_time", nullable = false)
    private LocalTime showTime;

    //fk -> movie(N:1) : nhiều xuất chiếu thuộc 1 phim
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id",  nullable = false,foreignKey = @ForeignKey(name = "fk_showtime_movie_id" ) )
    private Movie  movie;

    //fk -> cinema_room(n:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_showtime_cinema_room_id"))
    private CinemaRoom cinemaRoom;

    //quan hệ ngược -> scheduleSeat
    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleSeat> scheduleSeats;
    
}
