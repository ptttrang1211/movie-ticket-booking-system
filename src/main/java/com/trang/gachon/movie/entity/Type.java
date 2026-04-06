package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.MovieGenre;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Type {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

    // Tên hiển thị: "Hành động", "Kinh dị"...
    @Column(name = "type_name", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String typeName;

    // Enum để dùng trong code logic, lưu dạng string vào DB
    @Enumerated(EnumType.STRING)
    @Column(name = "movie_genre", length = 30)
    private MovieGenre genre;
}
