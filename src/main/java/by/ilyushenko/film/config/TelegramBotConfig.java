package by.ilyushenko.film.config;

import by.ilyushenko.film.telegram.FilmTelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Data
public class TelegramBotConfig {
    private final FilmTelegramBot bot;

    public TelegramBotConfig(FilmTelegramBot bot) {
        this.bot = bot;
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Телеграм бот успешно зарегистрирован!");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при регистрации бота: " + e.getMessage());
        }
    }
}
