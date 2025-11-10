package by.ilyushenko.film.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {
    
    private boolean ok;
    
    @JsonProperty("description")
    private List<MovieSearchResult> description;
    
    @JsonProperty("error_code")
    private int error_code;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieSearchResult {
        
        @JsonProperty("#TITLE")
        private String title;
        
        @JsonProperty("#YEAR")
        private Integer year;
        
        @JsonProperty("#IMDB_ID")
        private String imdbId;
        
        @JsonProperty("#RANK")
        private Integer rank;
        
        @JsonProperty("#ACTORS")
        private String actors;
        
        @JsonProperty("#AKA")
        private String aka;
        
        @JsonProperty("#IMG_POSTER")
        private String poster;
    }
}
