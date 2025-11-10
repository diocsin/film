package by.ilyushenko.film.dto;

import lombok.Data;

@Data
public class MovieSearchRequest {
    private String title;
    private Integer year;
    private String imdbId;
}

