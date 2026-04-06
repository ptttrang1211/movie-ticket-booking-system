package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.SeatType;
import jakarta.persistence.FetchType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    // hàng: A, B, C...
    @Column(name = "seat_row", nullable = false, length = 5)
    private String seatRow;

    // số ghế: 1, 2, 3...
    @Column(name = "seat_column", nullable = false)
    private Integer seatColumn;
    
    //db lưu string  "normal" | "vip"
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type")
    private SeatType seatType = SeatType.NORMAL;

    // ---- FK → cinema_room (N:1)     1 phòng có nhiều ghế
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_room_id", nullable = false,  foreignKey = @ForeignKey(name = "fk_seat_cinema_room_id"))
    private CinemaRoom cinemaRoom;
}
