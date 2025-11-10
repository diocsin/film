package by.ilyushenko.film.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailedMovieResponse {
    
    private boolean ok;
    private int error_code;
    private String description;
    
    @JsonProperty("short")
    private MovieDetails shortDetails;
    
    private String imdbId;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieDetails {
        private String name;
        private String alternateName;
        private String image;
        private String description;
        
        @JsonProperty("aggregateRating")
        private Rating aggregateRating;
        
        private String contentRating;
        private String[] genre;
        private String datePublished;
        private String keywords;
        
        @JsonProperty("creator")
        private Creator[] creators;
        
        @JsonProperty("duration")
        private String duration;
        
        @JsonProperty("actors")
        private Object actors;
        
        @JsonProperty("director")
        private Object director;
        
        @JsonProperty("writer")
        private Object writer;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rating {
        private Double ratingValue;
        private Integer ratingCount;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator {
        private String url;
    }
    
    // Top-level fields with cast, director, writer
    @JsonProperty("top")
    private TopLevelDetails top;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TopLevelDetails {
        @JsonProperty("principalCreditsV2")
        private PrincipalCredits[] principalCreditsV2;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PrincipalCredits {
        @JsonProperty("grouping")
        private Grouping grouping;
        
        @JsonProperty("credits")
        private Credit[] credits;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Grouping {
        @JsonProperty("text")
        private String text;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Credit {
        @JsonProperty("name")
        private CreditName name;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditName {
        @JsonProperty("nameText")
        private NameText nameText;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NameText {
        @JsonProperty("text")
        private String text;
    }
}

