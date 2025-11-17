package by.ilyushenko.film.service;

import by.ilyushenko.film.dto.MovieEvent;

public interface MovieEventProducer {
    void sendMovieSavedEvent(MovieEvent event);
}

