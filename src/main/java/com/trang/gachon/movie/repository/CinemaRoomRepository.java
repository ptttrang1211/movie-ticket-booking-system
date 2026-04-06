package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom,Long> {

    List<CinemaRoom> findByBranch_BranchIdOrderByCinemaRoomNameAsc(Long branchId);

    boolean existsByBranch_BranchIdAndCinemaRoomNameIgnoreCase(Long branchId, String cinemaRoomName);

    boolean existsByBranch_BranchIdAndCinemaRoomNameIgnoreCaseAndCinemaRoomIdNot(
            Long branchId, String cinemaRoomName, Long cinemaRoomId
    );
}
