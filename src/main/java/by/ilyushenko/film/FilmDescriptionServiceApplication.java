package by.ilyushenko.film;

import by.ilyushenko.film.config.TelegramBotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class FilmDescriptionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FilmDescriptionServiceApplication.class, args);
	}

}
