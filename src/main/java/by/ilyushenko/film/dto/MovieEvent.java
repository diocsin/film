package by.ilyushenko.film.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieEvent implements Serializable {
    private Long id;
    private String imdbId;
    private String title;
    private Integer releaseYear;
    private String rating;
    private String plot;
    private String director;
    private String actors;
    private String genres;
    private String poster;
    private String released;
    private String runtime;
    private String imdbRating;
    private String boxOffice;
    private String production;
    private String writer;
    private String language;
    private String country;
    private String awards;
    private LocalDateTime savedAt;
    private LocalDateTime eventTimestamp;
}

