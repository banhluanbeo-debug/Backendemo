package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Movie;
import java.util.List;

public interface MovieService {

    List<Movie> getAll();

    Movie getById(Long id);

    Movie create(Movie movie);

    Movie update(Long id, Movie movie);

    void delete(Long id);
    
    List<Movie> getComingSoonMovies();

    List<Movie> getNowShowingMovies();
}
