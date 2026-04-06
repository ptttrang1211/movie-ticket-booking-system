package com.trang.gachon.movie.entity;

// SCHEDULE_SEAT: trạng thái từng ghế trong 1 suất chiếu
//
// Trước: FK riêng lẻ (seat_id + schedule_id + movie_id + show_date)
// Sau:   FK → Showtime (gộp movie + cinema_room + date + time)
//
// Ví dụ:
//   Ghế 1A | Showtime(Doctor Strange, Room1, 12/01, 21:00) → SOLD
//   Ghế 1A | Showtime(Doctor Strange, Room1, 13/01, 21:00) → AVAILABLE
//   Ghế 1A | Showtime(Avengers, Room2, 12/01, 21:00)       → AVAILABLE

import com.trang.gachon.movie.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "schedule_seat",
        uniqueConstraints = @UniqueConstraint(
                // 1 ghế chỉ có 1 trạng thái trong 1 suất chiếu
                name = "uk_schedule_seat_unique",
                columnNames = {"seat_id", "showtime_id"}
        ),
        indexes = {
                @Index(name = "idx_ss_showtime_id",  columnList = "showtime_id"),
                @Index(name = "idx_ss_seat_status",  columnList = "seat_status")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_seat_id")
    private Long scheduleSeatId;

    //db lưu string available, selecting,sold
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_status", nullable = false, length = 20)
    private SeatStatus seatStatus = SeatStatus.AVAILABLE;

    //fk -> seat(N :1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ss_seat_id"))
    private Seat seat;


//    //fk -> movie (N:1)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "movie_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ss_movie_id"))
//    private Movie  movie;


//    //fk -> schedule : 1 schedule có nhiều scheduleSeat
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "schedule_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ss_schedule_id"))
//    private Schedule schedule;

    // ---- FK → showtime (N:1) ----
    // Showtime đã chứa: movie + cinemaRoom + showDate + showTime
    // → không cần FK riêng nữa
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "showtime_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ss_showtime_id")
    )
    private Showtime showtime;


}
