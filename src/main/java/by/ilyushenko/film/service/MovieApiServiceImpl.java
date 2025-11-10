package by.ilyushenko.film.service;

import by.ilyushenko.film.dto.MovieApiResponse;
import by.ilyushenko.film.dto.SearchResponse;
import by.ilyushenko.film.dto.DetailedMovieResponse;
import by.ilyushenko.film.model.Movie;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieApiServiceImpl implements MovieApiService {

    private final RestTemplate restTemplate;

    @Value("${movie.api.base-url}")
    private String baseUrl;

    @Override
    public MovieApiResponse searchByTitle(String title) {
        String url = baseUrl + "?q=" + encodeUrl(title);
        return fetchMovie(url);
    }

    @Override
    public MovieApiResponse searchByTitleAndYear(String title, Integer year) {
        String url = baseUrl + "?q=" + encodeUrl(title + " " + year);
        return fetchMovie(url);
    }

    @Override
    public MovieApiResponse searchByImdbId(String imdbId) {
        String url = baseUrl + "?q=" + imdbId;
        return fetchMovie(url);
    }

    private MovieApiResponse fetchMovie(String url) {
        try {
            log.info("Fetching movie from URL: {}", url);
            SearchResponse searchResponse = restTemplate.getForObject(url, SearchResponse.class);
            
            if (searchResponse == null || !searchResponse.isOk() || 
                searchResponse.getDescription() == null || searchResponse.getDescription().isEmpty()) {
                throw new RuntimeException("Movie not found");
            }
            
            // Берем первый результат из списка
            SearchResponse.MovieSearchResult firstResult = searchResponse.getDescription().get(0);
            
            // Получаем подробную информацию по IMDb ID
            String detailUrl = baseUrl.replace("/search", "") + "/search?tt=" + firstResult.getImdbId();
            log.info("Fetching detailed movie info from URL: {}", detailUrl);
            
            // Пытаемся получить детальную информацию
            try {
                DetailedMovieResponse detailResponse = restTemplate.getForObject(detailUrl, DetailedMovieResponse.class);
                if (detailResponse != null && detailResponse.isOk() && detailResponse.getShortDetails() != null) {
                    DetailedMovieResponse.MovieDetails details = detailResponse.getShortDetails();
                    
                    MovieApiResponse response = new MovieApiResponse();
                    response.setTitle(details.getName());
                    response.setYear((details.getDatePublished() != null && details.getDatePublished().length() >= 4) ? details.getDatePublished().substring(0, 4) : String.valueOf(firstResult.getYear()));
                    response.setImdbID(firstResult.getImdbId());
                    response.setPoster(details.getImage());
                    response.setPlot(details.getDescription());
                    
                    if (details.getGenre() != null && details.getGenre().length > 0) {
                        response.setGenre(String.join(", ", details.getGenre()));
                    }
                    
                    if (details.getAggregateRating() != null) {
                        response.setImdbRating(String.valueOf(details.getAggregateRating().getRatingValue()));
                    }
                    
                    log.info("Content rating: {}", details.getContentRating());
                    log.info("Duration: {}", details.getDuration());
                    log.info("Keywords: {}", details.getKeywords());
                    
                    response.setRated(details.getContentRating());
                    
                    // Обрабатываем duration из формата "PT2H35M"
                    if (details.getDuration() != null) {
                        response.setRuntime(formatDuration(details.getDuration()));
                    }
                    
                    // Keywords в поле Writer
                    if (details.getKeywords() != null && !details.getKeywords().isEmpty()) {
                        response.setWriter(details.getKeywords());
                    }
                    
                    // Извлекаем актеров, режиссера и продюсеров
                    if (detailResponse.getTop() != null && detailResponse.getTop().getPrincipalCreditsV2() != null) {
                        List<String> actors = new ArrayList<>();
                        List<String> directors = new ArrayList<>();
                        
                        for (DetailedMovieResponse.PrincipalCredits credits : detailResponse.getTop().getPrincipalCreditsV2()) {
                            String groupText = credits.getGrouping() != null ? credits.getGrouping().getText() : "";
                            
                            if ("Stars".equals(groupText) || "Cast".equals(groupText)) {
                                if (credits.getCredits() != null) {
                                    for (DetailedMovieResponse.Credit credit : credits.getCredits()) {
                                        if (credit.getName() != null && credit.getName().getNameText() != null) {
                                            actors.add(credit.getName().getNameText().getText());
                                        }
                                    }
                                }
                            } else if ("Director".equals(groupText) || "Directors".equals(groupText)) {
                                if (credits.getCredits() != null) {
                                    for (DetailedMovieResponse.Credit credit : credits.getCredits()) {
                                        if (credit.getName() != null && credit.getName().getNameText() != null) {
                                            directors.add(credit.getName().getNameText().getText());
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (!actors.isEmpty()) {
                            response.setActors(String.join(", ", actors));
                        }
                        if (!directors.isEmpty()) {
                            response.setDirector(String.join(", ", directors));
                        }
                    }
                    
                    response.setResponse("True");
                    
                    log.info("Successfully fetched detailed movie info: {}", response.getTitle());
                    return response;
                }
            } catch (Exception e) {
                log.warn("Could not fetch detailed info, using basic info: {}", e.getMessage());
            }
            
            // Если детальная информация недоступна, используем базовую
            MovieApiResponse response = new MovieApiResponse();
            response.setTitle(firstResult.getTitle());
            response.setYear(String.valueOf(firstResult.getYear()));
            response.setImdbID(firstResult.getImdbId());
            response.setActors(firstResult.getActors());
            response.setPoster(firstResult.getPoster());
            response.setResponse("True");
            
            log.info("Successfully fetched movie (basic info): {}", response.getTitle());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling API: {}", e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new RuntimeException("Movie not found");
            }
            throw new RuntimeException("Error calling external API: " + e.getMessage(), e);
        } catch (org.springframework.web.client.RestClientException e) {
            log.error("RestClient exception: {}", e.getMessage());
            throw new RuntimeException("Could not fetch movie from external API. Please check the movie title or try again later.");
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public Movie convertToMovie(MovieApiResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }

        Movie movie = new Movie();
        movie.setImdbId(response.getImdbID());
        movie.setTitle(response.getTitle());
        movie.setReleaseYear(parseYear(response.getYear()));
        movie.setRating(response.getRated());
        movie.setPlot(response.getPlot());
        movie.setDirector(response.getDirector());
        movie.setActors(response.getActors());
        movie.setGenres(response.getGenre());
        movie.setPoster(response.getPoster());
        movie.setReleased(response.getReleased());
        movie.setRuntime(response.getRuntime());
        movie.setImdbRating(response.getImdbRating());
        movie.setBoxOffice(response.getBoxOffice());
        movie.setProduction(response.getProduction());
        movie.setWriter(response.getWriter());
        movie.setLanguage(response.getLanguage());
        movie.setCountry(response.getCountry());
        movie.setAwards(response.getAwards());

        return movie;
    }

    private String encodeUrl(String text) {
        try {
            return java.net.URLEncoder.encode(text, "UTF-8");
        } catch (Exception e) {
            return text.replace(" ", "+");
        }
    }

    private Integer parseYear(String yearStr) {
        if (yearStr == null || yearStr.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(yearStr.trim().split("–")[0].split("-")[0]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String formatDuration(String duration) {
        if (duration == null || duration.isEmpty()) {
            return null;
        }
        
        // Формат "PT2H35M" -> "2 ч 35 мин"
        duration = duration.replace("PT", "");
        
        int hours = 0;
        int minutes = 0;
        
        if (duration.contains("H")) {
            String[] parts = duration.split("H");
            try {
                hours = Integer.parseInt(parts[0]);
                if (parts.length > 1) {
                    duration = parts[1];
                } else {
                    duration = "";
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        if (duration.contains("M")) {
            String[] parts = duration.split("M");
            try {
                minutes = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        if (hours > 0 && minutes > 0) {
            return hours + " ч " + minutes + " мин";
        } else if (hours > 0) {
            return hours + " ч";
        } else if (minutes > 0) {
            return minutes + " мин";
        }
        
        return duration;
    }
    
    @Override
    public SearchResponse.MovieSearchResult[] searchMoviesList(String title) {
        String url = baseUrl + "?q=" + encodeUrl(title);
        log.info("Searching movies list from URL: {}", url);
        
        try {
            SearchResponse searchResponse = restTemplate.getForObject(url, SearchResponse.class);
            
            if (searchResponse == null || !searchResponse.isOk() || 
                searchResponse.getDescription() == null || searchResponse.getDescription().isEmpty()) {
                throw new RuntimeException("Movies not found");
            }
            
            log.info("Found {} movies", searchResponse.getDescription().size());
            return searchResponse.getDescription().toArray(new SearchResponse.MovieSearchResult[0]);
        } catch (Exception e) {
            log.error("Error fetching movies list: {}", e.getMessage());
            throw new RuntimeException("Could not fetch movies list: " + e.getMessage(), e);
        }
    }
    
    @Override
    public MovieApiResponse getMovieDetails(String imdbId) {
        String detailUrl = baseUrl.replace("/search", "") + "/search?tt=" + imdbId;
        log.info("Fetching detailed movie info from URL: {}", detailUrl);
        
        try {
            DetailedMovieResponse detailResponse = restTemplate.getForObject(detailUrl, DetailedMovieResponse.class);
            
            if (detailResponse == null || !detailResponse.isOk() || detailResponse.getShortDetails() == null) {
                throw new RuntimeException("Movie details not found");
            }
            
            DetailedMovieResponse.MovieDetails details = detailResponse.getShortDetails();
            
            MovieApiResponse response = new MovieApiResponse();
            response.setTitle(details.getName());
            response.setYear((details.getDatePublished() != null && details.getDatePublished().length() >= 4) ? 
                    details.getDatePublished().substring(0, 4) : "N/A");
            response.setImdbID(imdbId);
            response.setPoster(details.getImage());
            response.setPlot(details.getDescription());
            
            if (details.getGenre() != null && details.getGenre().length > 0) {
                response.setGenre(String.join(", ", details.getGenre()));
            }
            
            if (details.getAggregateRating() != null) {
                response.setImdbRating(String.valueOf(details.getAggregateRating().getRatingValue()));
            }
            
            response.setRated(details.getContentRating());
            
            if (details.getDuration() != null) {
                response.setRuntime(formatDuration(details.getDuration()));
            }
            
            if (details.getKeywords() != null && !details.getKeywords().isEmpty()) {
                response.setWriter(details.getKeywords());
            }
            
            // Извлекаем актеров и режиссера
            if (detailResponse.getTop() != null && detailResponse.getTop().getPrincipalCreditsV2() != null) {
                List<String> actors = new ArrayList<>();
                List<String> directors = new ArrayList<>();
                
                for (DetailedMovieResponse.PrincipalCredits credits : detailResponse.getTop().getPrincipalCreditsV2()) {
                    String groupText = credits.getGrouping() != null ? credits.getGrouping().getText() : "";
                    
                    if ("Stars".equals(groupText) || "Cast".equals(groupText)) {
                        if (credits.getCredits() != null) {
                            for (DetailedMovieResponse.Credit credit : credits.getCredits()) {
                                if (credit.getName() != null && credit.getName().getNameText() != null) {
                                    actors.add(credit.getName().getNameText().getText());
                                }
                            }
                        }
                    } else if ("Director".equals(groupText) || "Directors".equals(groupText)) {
                        if (credits.getCredits() != null) {
                            for (DetailedMovieResponse.Credit credit : credits.getCredits()) {
                                if (credit.getName() != null && credit.getName().getNameText() != null) {
                                    directors.add(credit.getName().getNameText().getText());
                                }
                            }
                        }
                    }
                }
                
                if (!actors.isEmpty()) {
                    response.setActors(String.join(", ", actors));
                }
                if (!directors.isEmpty()) {
                    response.setDirector(String.join(", ", directors));
                }
            }
            
            response.setResponse("True");
            
            log.info("Successfully fetched detailed movie info: {}", response.getTitle());
            return response;
        } catch (Exception e) {
            log.error("Error fetching movie details: {}", e.getMessage());
            throw new RuntimeException("Could not fetch movie details: " + e.getMessage(), e);
        }
    }
}
