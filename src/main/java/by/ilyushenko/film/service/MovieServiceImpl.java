package by.ilyushenko.film.service;

import by.ilyushenko.film.dto.MovieEvent;
import by.ilyushenko.film.model.Movie;
import by.ilyushenko.film.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieEventProducer movieEventProducer;

    @Override
    @Transactional
    public Movie saveMovie(Movie movie) {
        if (movieRepository.existsByImdbId(movie.getImdbId())) {
            throw new IllegalArgumentException("Movie with IMDb ID " + movie.getImdbId() + " already exists");
        }
        Movie saved = movieRepository.save(movie);
        
        // Отправка события в очередь после успешного сохранения
        try {
            MovieEvent event = convertToEvent(saved);
            movieEventProducer.sendMovieSavedEvent(event);
        } catch (Exception e) {
            log.error("Failed to send movie event, but movie was saved: {}", saved.getTitle(), e);
            // Не прерываем транзакцию, если отправка события не удалась
        }
        
        return saved;
    }
    
    private MovieEvent convertToEvent(Movie movie) {
        return MovieEvent.builder()
                .id(movie.getId())
                .imdbId(movie.getImdbId())
                .title(movie.getTitle())
                .releaseYear(movie.getReleaseYear())
                .rating(movie.getRating())
                .plot(movie.getPlot())
                .director(movie.getDirector())
                .actors(movie.getActors())
                .genres(movie.getGenres())
                .poster(movie.getPoster())
                .released(movie.getReleased())
                .runtime(movie.getRuntime())
                .imdbRating(movie.getImdbRating())
                .boxOffice(movie.getBoxOffice())
                .production(movie.getProduction())
                .writer(movie.getWriter())
                .language(movie.getLanguage())
                .country(movie.getCountry())
                .awards(movie.getAwards())
                .savedAt(movie.getSavedAt())
                .eventTimestamp(LocalDateTime.now())
                .build();
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

