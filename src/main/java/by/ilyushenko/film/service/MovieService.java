package by.ilyushenko.film.service;

import by.ilyushenko.film.model.Movie;

import java.util.List;

public interface MovieService {
    Movie saveMovie(Movie movie);
    List<Movie> getAllSavedMovies();
    void deleteMovie(Long id);
    boolean isMovieSaved(String imdbId);
    Movie getMovieById(Long id);
}

