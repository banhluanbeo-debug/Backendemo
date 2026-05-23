package com.tranvanluan.backend.service.mapper;

import com.tranvanluan.backend.dto.MovieDTO;
import com.tranvanluan.backend.dto.ShowtimeDTO;
import com.tranvanluan.backend.entity.Movie;
import com.tranvanluan.backend.entity.Showtime;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieMapper {

    public MovieDTO toDTO(Movie movie) {
        List<ShowtimeDTO> showtimes = movie.getShowtimes() != null
                ? movie.getShowtimes().stream().map(this::toDTO).collect(Collectors.toList())
                : List.of();
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .content(movie.getContent())
                .duration(movie.getDuration())
                .ageLimit(movie.getAgeLimit())
                .endDate(movie.getEndDate())

                .posterUrl(movie.getPosterUrl())
                .backdropUrl(movie.getBackdropUrl())
                .trailerUrl(movie.getTrailerUrl())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .genre(movie.getGenre())
                .language(movie.getLanguage())
                .subtitle(movie.getSubtitle())
                .releaseDate(movie.getReleaseDate())

                .showtimes(showtimes)
                .build();
    }

    public ShowtimeDTO toDTO(Showtime showtime) {
        return ShowtimeDTO.builder()
                .id(showtime.getId())
                .showDate(showtime.getShowDate())
                .showTime(showtime.getShowTime())
                .price(showtime.getPrice())
                .roomId(showtime.getRoom().getId())
                .roomName(showtime.getRoom().getName())
                .build();
    }
}
