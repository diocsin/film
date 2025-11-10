package by.ilyushenko.film.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieSearchResultResponse {
    
    private String title;
    private Integer year;
    private String imdbId;
    private Integer rank;
    private String actors;
    private String aka;
    private String poster;
    
    public static MovieSearchResultResponse from(SearchResponse.MovieSearchResult result) {
        MovieSearchResultResponse response = new MovieSearchResultResponse();
        response.setTitle(result.getTitle());
        response.setYear(result.getYear());
        response.setImdbId(result.getImdbId());
        response.setRank(result.getRank());
        response.setActors(result.getActors());
        response.setAka(result.getAka());
        response.setPoster(result.getPoster());
        return response;
    }
}

