package com.tranvanluan.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer duration;

    private String ageLimit;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String posterUrl;

    private String backdropUrl;

    private String trailerUrl;

    private String director;

    private String cast;

    private String genre;

    private String language;

    private String subtitle;

    private LocalDate releaseDate;

    private LocalDate endDate;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Showtime> showtimes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}