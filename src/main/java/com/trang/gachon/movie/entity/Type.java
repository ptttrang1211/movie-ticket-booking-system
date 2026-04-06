package com.trang.gachon.movie.entity;

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

    // hành động, hài hước, viễn tưởng...
    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;
}
