package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    //phim đang chiếu: fromDate <= hôm nay <= toDate
    List<Movie> findByFromDateLessThanEqualAndToDateGreaterThanEqual(LocalDate today1, LocalDate today2);

    //tìm kiếm theo tên tiếg anh or tiếng việt
    @Query("SELECT m FROM Movie m WHERE " +
            "LOWER(m.movieNameEnglish) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(m.movieNameVn) LIKE LOWER(CONCAT('%', :kw, '%'))")
    List<Movie> searchByName(@Param("kw")  String kw);

    Optional<Movie> findFirstByFeaturedOnHomeTrue();
}
