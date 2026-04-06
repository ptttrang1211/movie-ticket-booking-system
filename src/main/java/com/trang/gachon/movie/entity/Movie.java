package com.trang.gachon.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(
        name = "movie",
        indexes = {
                @Index(name = "idx_movie_from_to_date", columnList = "from_date, to_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_name_english", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String movieNameEnglish;

    @Column(name = "movie_name_vn", columnDefinition = "NVARCHAR(255)")
    private String movieNameVn;

    @Column(name = "actor", columnDefinition = "NVARCHAR(500)")
    private String actor;

    @Column(name = "director", columnDefinition = "NVARCHAR(255)")
    private String director;

    @Column(name = "movie_production_company", columnDefinition = "NVARCHAR(255)")
    private String movieProductionCompany;

    //thời lượng phút
    @Column(name = "duration")
    private Integer duration;

    //2D, 3D, 4D
    @Column(name = "version", columnDefinition = "NVARCHAR(255)")
    private String version;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    //link trailer lấy trên youtube
    @Column(name = "trailer", columnDefinition = "NVARCHAR(500)")
    private String trailer;

    @Column(name = "review_link", columnDefinition = "NVARCHAR(500)")
    private String reviewLink;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    //ảnh poster lớn
    @Column(name = "large_image", columnDefinition = "NVARCHAR(1000)")
    private String largeImage;

    //ảnh poster nhở
    @Column(name = "small_image", columnDefinition = "NVARCHAR(1000)")
    private String smallImage;

    @Column(name = "banner_image", columnDefinition = "NVARCHAR(1000)")
    private String bannerImage;

    @Column(name = "featured_on_home", nullable = false)
    @Builder.Default
    private boolean featuredOnHome = false;

//    // ---- FK → cinema_room (N:1) ---- : nên thông qua showtime để lấy phòng chiếu, tránh ràng buộc cứng nhắc
//    // Phim mặc định chiếu tại phòng nào (admin cấu hình)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(
//            name = "cinema_room_id",
//            foreignKey = @ForeignKey(name = "fk_movie_cinema_room_id")
//    )
//    private CinemaRoom cinemaRoom;

    // ---- M:N → type — JPA tự tạo bảng movie_type ----
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_type",
            joinColumns        = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "type_id"),
            foreignKey         = @ForeignKey(name = "fk_mt_movie_id"),
            inverseForeignKey  = @ForeignKey(name = "fk_mt_type_id")
    )
    private List<Type> types;

// //sai logic: 1 movie chưa nhiều schedule ok: 1 schedule chứa nhiều movie thì vô lí
////    //M:N -> schedule - JPA tự tạo bảng movie_schedule
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(name = "movie_schedule",joinColumns        = @JoinColumn(name = "movie_id"),
//            inverseJoinColumns = @JoinColumn(name = "schedule_id"),
//            foreignKey         = @ForeignKey(name = "fk_ms_movie_id"),
//            inverseForeignKey  = @ForeignKey(name = "fk_ms_schedule_id"))
//    private List<Schedule>  schedules;

        //dùng để lấy all suất chiếu của 1 phim
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Showtime>  showTimes;

    @Transient
    public String getLargeImageSrc() {
        return resolveImagePath(largeImage);
    }

    @Transient
    public String getSmallImageSrc() {
        return resolveImagePath(smallImage);
    }

    @Transient
    public String getBannerImageSrc() {
        return resolveImagePath(bannerImage);
    }

    // Movie demo mới có thể dùng poster URL ngoài, còn admin upload vẫn đi qua uploads/.
    private String resolveImagePath(String imageValue) {
        if (!StringUtils.hasText(imageValue)) {
            return null;
        }
        if (imageValue.startsWith("http://") || imageValue.startsWith("https://") || imageValue.startsWith("//")) {
            return imageValue;
        }
        return "/uploads/" + imageValue;
    }

}
