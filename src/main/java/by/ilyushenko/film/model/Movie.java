package by.ilyushenko.film.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String imdbId;

    @Column(nullable = false)
    private String title;

    @Column(name = "release_year")
    private Integer releaseYear;

    private String rating;

    @Column(length = 1000)
    private String plot;

    private String director;

    @Column(length = 1000)
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

    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
    }
}

