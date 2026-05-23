package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.entity.Movie;
import com.tranvanluan.backend.repository.MovieRepository;
import com.tranvanluan.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final com.tranvanluan.backend.repository.OrderDetailRepository orderDetailRepository;

    @Override
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    @Override
    public Movie getById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Movie not found with id " + id));
    }

    @Override
    public Movie create(Movie movie) {
        validateMovie(movie);
        return movieRepository.save(movie);
    }

    @Override
    public Movie update(Long id, Movie movie) {
        validateMovie(movie);
        Movie existing = getById(id);

        existing.setTitle(movie.getTitle());
        existing.setContent(movie.getContent());
        existing.setDuration(movie.getDuration());
        existing.setAgeLimit(movie.getAgeLimit());

        existing.setPosterUrl(movie.getPosterUrl());
        existing.setBackdropUrl(movie.getBackdropUrl());
        existing.setTrailerUrl(movie.getTrailerUrl());

        existing.setDirector(movie.getDirector());
        existing.setCast(movie.getCast());
        existing.setGenre(movie.getGenre());
        existing.setLanguage(movie.getLanguage());
        existing.setSubtitle(movie.getSubtitle());
        existing.setReleaseDate(movie.getReleaseDate());
        existing.setEndDate(movie.getEndDate());

        return movieRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new NoSuchElementException("Movie not found with id " + id);
        }
        boolean hasOrders = orderDetailRepository.existsByShowtimeSeat_Showtime_MovieId(id);
        if (hasOrders) {
            throw new IllegalArgumentException("Không thể xóa phim vì đã có khách hàng đặt vé xem phim này!");
        }
        movieRepository.deleteById(id);
    }

    @Override
    public List<Movie> getNowShowingMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByReleaseDateLessThanEqualAndEndDateGreaterThanEqual(today, today);
    }

    @Override
    public List<Movie> getComingSoonMovies() {
        return movieRepository.findByReleaseDateAfter(LocalDate.now()); // ← sửa cái này luôn
    }

    private void validateMovie(Movie movie) {
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phim không được để trống!");
        }
        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0!");
        }
        if (movie.getReleaseDate() != null && movie.getEndDate() != null) {
            if (movie.getReleaseDate().isAfter(movie.getEndDate())) {
                throw new IllegalArgumentException("Ngày công chiếu không được sau ngày kết thúc!");
            }
        }
        if (movie.getPosterUrl() == null || movie.getPosterUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Poster phim không được để trống!");
        }
    }
}
