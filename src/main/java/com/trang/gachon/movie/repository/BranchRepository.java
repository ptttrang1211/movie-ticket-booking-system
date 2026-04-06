package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Branch;
import com.trang.gachon.movie.enums.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch,Long> {
    // Chỉ lấy chi nhánh đang hoạt động để hiển thị cho user chọn
    List<Branch> findByBranchStatusOrderByBranchNameAsc(BranchStatus branchStatus);

    List<Branch> findAllByOrderByBranchNameAsc();

    @Query("""
            select distinct b
            from Branch b
            join b.cinemaRooms cr
            join cr.showTimes st
            where b.branchStatus = :status
              and st.movie.movieId = :movieId
              and st.showDate >= :today
            order by b.branchName asc
            """)
    List<Branch> findActiveBranchesByMovie(
            @Param("movieId") Long movieId,
            @Param("today") LocalDate today,
            @Param("status") BranchStatus status
    );

    boolean existsByBranchNameIgnoreCase(String branchName);

    boolean existsByBranchNameIgnoreCaseAndBranchIdNot(String branchName, Long branchId);
}
