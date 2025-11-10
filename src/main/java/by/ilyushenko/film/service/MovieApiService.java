package by.ilyushenko.film.service;

import by.ilyushenko.film.dto.MovieApiResponse;
import by.ilyushenko.film.dto.SearchResponse;
import by.ilyushenko.film.model.Movie;

public interface MovieApiService {
    MovieApiResponse searchByTitle(String title);
    MovieApiResponse searchByTitleAndYear(String title, Integer year);
    MovieApiResponse searchByImdbId(String imdbId);
    Movie convertToMovie(MovieApiResponse response);
    
    // Методы для получения списка результатов поиска
    SearchResponse.MovieSearchResult[] searchMoviesList(String title);
    MovieApiResponse getMovieDetails(String imdbId);
}

