package com.trang.gachon.movie.entity;

import com.trang.gachon.movie.enums.BranchStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "branch")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Branch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;

    //cgv thăng long, fpt cimena hà nội...
    @Column(name = "branch_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String branchName;

    @Column(name = "address", nullable = false,columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "image", length = 255)
    private String image;

    //link iframe google máp embed - ko cần api key
    //lấy từ : google map -> share -> embed a map -> copy src =""
    @Column(name = "map_embed_url", columnDefinition = "NVARCHAR(1000)")
    private String mapEmbedUrl;

    //ACTIVE , INACTIVE
    @Enumerated(EnumType.STRING)
    @Column(name = "branch_status", nullable = false, length = 20)
    private BranchStatus  branchStatus = BranchStatus.ACTIVE;

    //quan hệ ngược
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    private List<CinemaRoom> cinemaRooms;

    
}
