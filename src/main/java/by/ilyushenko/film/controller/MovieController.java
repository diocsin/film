package by.ilyushenko.film.controller;

import by.ilyushenko.film.dto.MovieApiResponse;
import by.ilyushenko.film.dto.MovieSearchResultResponse;
import by.ilyushenko.film.model.Movie;
import by.ilyushenko.film.service.MovieApiService;
import by.ilyushenko.film.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieApiService movieApiService;
    private final MovieService movieService;

    @GetMapping("/search")
    public ResponseEntity<MovieApiResponse> searchMovie(
            @RequestParam String title,
            @RequestParam(required = false) Integer year) {
        
        log.info("Searching movie with title: {}, year: {}", title, year);
        
        MovieApiResponse response;
        if (year != null) {
            response = movieApiService.searchByTitleAndYear(title, year);
        } else {
            response = movieApiService.searchByTitle(title);
        }
        
        log.info("Search result: {}", response != null ? "Found" : "Not found");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/imdb")
    public ResponseEntity<MovieApiResponse> searchByImdbId(@RequestParam String id) {
        MovieApiResponse response = movieApiService.searchByImdbId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<Movie> saveMovie(@RequestBody MovieApiResponse movieResponse) {
        Movie movie = movieApiService.convertToMovie(movieResponse);
        Movie saved = movieService.saveMovie(movie);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/saved")
    public ResponseEntity<List<Movie>> getSavedMovies() {
        List<Movie> movies = movieService.getAllSavedMovies();
        return ResponseEntity.ok(movies);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved/{id}")
    public ResponseEntity<Movie> getSavedMovie(@PathVariable Long id) {
        Movie movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }
    
    @GetMapping("/search-list")
    public ResponseEntity<MovieSearchResultResponse[]> searchMoviesList(@RequestParam String title) {
        log.info("Searching movies list for: {}", title);
        var results = movieApiService.searchMoviesList(title);
        log.info("Found {} movies", results.length);
        
        MovieSearchResultResponse[] response = new MovieSearchResultResponse[results.length];
        for (int i = 0; i < results.length; i++) {
            response[i] = MovieSearchResultResponse.from(results[i]);
        }
        
        if (results.length > 0) {
            log.info("First movie: title={}, year={}, actors={}", response[0].getTitle(), response[0].getYear(), response[0].getActors());
        }
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/details")
    public ResponseEntity<MovieApiResponse> getMovieDetails(@RequestParam String imdbId) {
        log.info("Getting movie details for IMDb ID: {}", imdbId);
        var details = movieApiService.getMovieDetails(imdbId);
        return ResponseEntity.ok(details);
    }
}
