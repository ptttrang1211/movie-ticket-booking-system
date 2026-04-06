package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    // Chỉ lấy KM còn hiệu lực để hiển thị trang public
    @Query("""
            select p
            from Promotion p
            where (p.startTime is null or p.startTime <= :now)
              and (p.endTime is null or p.endTime >= :now)
            order by p.startTime desc
            """)
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
}
