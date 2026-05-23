package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.MovieDTO;
import com.tranvanluan.backend.entity.Movie;
import com.tranvanluan.backend.service.MovieService;
import com.tranvanluan.backend.service.mapper.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final MovieMapper movieMapper;

    @GetMapping
    public List<MovieDTO> getAllMovies() {
        return movieService.getAll()
                .stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MovieDTO getMovieById(@PathVariable Long id) {
        return movieMapper.toDTO(movieService.getById(id));
    }

    @PostMapping
    public MovieDTO createMovie(@RequestBody Movie movie) {
        return movieMapper.toDTO(movieService.create(movie));
    }

    @PutMapping("/{id}")
    public MovieDTO updateMovie(@PathVariable Long id, @RequestBody Movie movie) {
        return movieMapper.toDTO(movieService.update(id, movie));
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable Long id) {
        movieService.delete(id);
    }

    @GetMapping("/coming-soon")
    public List<MovieDTO> comingSoon() {
        return movieService.getComingSoonMovies()
                .stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/now-showing")
    public List<MovieDTO> nowShowing() {
        return movieService.getNowShowingMovies()
                .stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }
}