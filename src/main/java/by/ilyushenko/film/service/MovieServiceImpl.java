package by.ilyushenko.film.service;

import by.ilyushenko.film.model.Movie;
import by.ilyushenko.film.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Movie saveMovie(Movie movie) {
        if (movieRepository.existsByImdbId(movie.getImdbId())) {
            throw new IllegalArgumentException("Movie with IMDb ID " + movie.getImdbId() + " already exists");
        }
        return movieRepository.save(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movie> getAllSavedMovies() {
        return movieRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie with ID " + id + " not found");
        }
        movieRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMovieSaved(String imdbId) {
        return movieRepository.existsByImdbId(imdbId);
    }

    @Override
    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie with ID " + id + " not found"));
    }
}

