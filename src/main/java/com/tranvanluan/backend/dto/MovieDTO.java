package com.tranvanluan.backend.dto;

import lombok.*;

import java.util.List;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDTO {

    private Long id;
    private String title;
    private String content;
    private Integer duration;
    private String ageLimit;

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

    private List<ShowtimeDTO> showtimes;
}
