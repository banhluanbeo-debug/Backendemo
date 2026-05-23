package com.tranvanluan.backend.repository;

import com.tranvanluan.backend.entity.Movie;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByReleaseDateAfter(LocalDate date);

    List<Movie> findByReleaseDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate releaseDate, LocalDate endDate);
}
