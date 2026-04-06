package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.util.List;

@Entity
@Table(
        name = "movie",
        indexes = {
                @Index(name = "idx_movie_cinema_room_id", columnList = "cinema_room_id"),
                @Index(name = "idx_movie_from_to_date",   columnList = "from_date, to_date")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_name_english", nullable = false, length = 255)
    private String movieNameEnglish;

    @Column(name = "movie_name_vn", length = 255)
    private String movieNameVn;

    @Column(name = "actor", length = 500)
    private String actor;

    @Column(name = "director", length = 255)
    private String director;

    @Column(name = "movie_production_company", length = 255)
    private String movieProductionCompany;

    //thời lượng phút
    @Column(name = "duration")
    private Integer duration;

    //2D, 3D, 4D
    @Column(name = "version", length = 255)
    private String version;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    //link trailer lấy trên youtube
    @Column(name = "trailer", length = 500)
    private String trailer;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    //ảnh poster lớn
    @Column(name = "large_image", length = 255)
    private String largeImage;

    //ảnh poster nhở
    @Column(name = "small_image", length = 255)
    private String smallImage;

    //FK   -> cinema_room (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_room_id", foreignKey = @ForeignKey(name = "fk_movie_cinema_room_id"))
    private CinemaRoom  cinemaRoom;

    // ---- M:N → type — JPA tự tạo bảng movie_type ----
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "movie_type",
            joinColumns        = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id"),
            foreignKey         = @ForeignKey(name = "fk_mt_movie_id"),
            inverseForeignKey  = @ForeignKey(name = "fk_mt_type_id"))
    private List<Type> types;


    //M:N -> schedule - JPA tự tạo bảng movie_schedule
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "movie_schedule",joinColumns        = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "schedule_id"),
            foreignKey         = @ForeignKey(name = "fk_ms_movie_id"),
            inverseForeignKey  = @ForeignKey(name = "fk_ms_schedule_id"))
    private List<Schedule>  schedules;


}
