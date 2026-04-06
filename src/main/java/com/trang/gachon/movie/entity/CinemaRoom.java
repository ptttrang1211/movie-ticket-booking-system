package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;


import java.util.List;

@Entity
@Table(name = "cinema_room")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CinemaRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cinema_room_id")
    private Long cinemaRoomId;

    @Column(name = "cinema_room_name", nullable = false, length = 100)
    private String cinemaRoomName;

    @Column(name = "seat_quantity", nullable = false)
    private Integer seatQuantity;

    @OneToMany(mappedBy = "cinemaRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;

    @OneToMany(mappedBy = "cinemaRoom",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Movie> movies;

    //fk -> branch (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id",  nullable = false,  foreignKey = @ForeignKey(name = "fk_cinema_room_branch_id"))
    private Branch branch;

    
}
