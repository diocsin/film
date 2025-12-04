package by.ilyushenko.film.telegram;

import by.ilyushenko.film.dto.MovieApiResponse;
import by.ilyushenko.film.dto.SearchResponse;
import by.ilyushenko.film.model.Movie;
import by.ilyushenko.film.service.MovieApiService;
import by.ilyushenko.film.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FilmTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final MovieService movieService;
    private final MovieApiService movieApiService;

    public FilmTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            MovieService movieService, MovieApiService movieApiService) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.movieService = movieService;
        this.movieApiService = movieApiService;
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start") || messageText.equals("/help")) {
                sendWelcomeMessage(chatId);
            } else if (messageText.startsWith("/search ")) {
                String searchQuery = messageText.substring(8).trim();
                if (searchQuery.isEmpty()) {
                    sendMessage(chatId, "❌ Пожалуйста, укажите название фильма для поиска.\nПример: /search Matrix");
                } else {
                    sendSearchResults(chatId, searchQuery);
                }
            } else if (messageText.equals("/movies") || messageText.equals("/list")) {
                sendMoviesList(chatId);
            } else {
                sendMessage(chatId, "Используйте /movies для просмотра списка фильмов, /search <название> для поиска или /help для справки.");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.startsWith("movie_")) {
                Long movieId = Long.parseLong(callbackData.substring(6));
                sendMovieDetails(chatId, movieId);
            } else if (callbackData.startsWith("search_")) {
                String imdbId = callbackData.substring(7);
                sendSearchMovieDetails(chatId, imdbId);
            }
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = """
                🎬 Добро пожаловать в бот фильмов!
                
                Доступные команды:
                /movies или /list - показать список сохраненных фильмов
                /search <название> - поиск фильмов по названию
                /help - показать эту справку
                
                Используйте кнопки для просмотра подробной информации о фильмах.
                """;
        sendMessage(chatId, welcomeText);
    }

    private void sendMoviesList(Long chatId) {
        List<Movie> movies = movieService.getAllSavedMovies();

        if (movies.isEmpty()) {
            sendMessage(chatId, "📭 Список фильмов пуст. Сохраните фильмы через API, чтобы они появились здесь.");
            return;
        }

        String messageText = "🎬 Список сохраненных фильмов:\n\nВыберите фильм для просмотра подробной информации:";

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Movie movie : movies) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            String buttonText = movie.getTitle();
            if (movie.getReleaseYear() != null) {
                buttonText += " (" + movie.getReleaseYear() + ")";
            }
            button.setText(buttonText);
            button.setCallbackData("movie_" + movie.getId());
            row.add(button);
            keyboard.add(row);
        }

        keyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(messageText);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending movies list", e);
        }
    }

    private void sendMovieDetails(Long chatId, Long movieId) {
        try {
            Movie movie = movieService.getMovieById(movieId);

            StringBuilder details = new StringBuilder();
            details.append("🎬 ").append(movie.getTitle()).append("\n\n");

            if (movie.getReleaseYear() != null) {
                details.append("📅 Год выпуска: ").append(movie.getReleaseYear()).append("\n");
            }

            if (movie.getReleased() != null && !movie.getReleased().isEmpty()) {
                details.append("📆 Дата выхода: ").append(movie.getReleased()).append("\n");
            }

            if (movie.getRuntime() != null && !movie.getRuntime().isEmpty()) {
                details.append("⏱ Длительность: ").append(movie.getRuntime()).append("\n");
            }

            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                details.append("🎭 Жанры: ").append(movie.getGenres()).append("\n");
            }

            if (movie.getRating() != null && !movie.getRating().isEmpty()) {
                details.append("📊 Рейтинг: ").append(movie.getRating()).append("\n");
            }

            if (movie.getImdbRating() != null && !movie.getImdbRating().isEmpty()) {
                details.append("⭐ IMDb рейтинг: ").append(movie.getImdbRating()).append("\n");
            }

            if (movie.getDirector() != null && !movie.getDirector().isEmpty()) {
                details.append("🎬 Режиссер: ").append(movie.getDirector()).append("\n");
            }

            if (movie.getWriter() != null && !movie.getWriter().isEmpty()) {
                details.append("✍️ Сценарист: ").append(movie.getWriter()).append("\n");
            }

            if (movie.getActors() != null && !movie.getActors().isEmpty()) {
                details.append("👥 Актёры: ").append(movie.getActors()).append("\n");
            }

            if (movie.getPlot() != null && !movie.getPlot().isEmpty()) {
                details.append("\n📖 Описание:\n").append(movie.getPlot()).append("\n");
            }

            if (movie.getLanguage() != null && !movie.getLanguage().isEmpty()) {
                details.append("\n🌐 Язык: ").append(movie.getLanguage()).append("\n");
            }

            if (movie.getCountry() != null && !movie.getCountry().isEmpty()) {
                details.append("🌍 Страна: ").append(movie.getCountry()).append("\n");
            }

            if (movie.getBoxOffice() != null && !movie.getBoxOffice().isEmpty()) {
                details.append("💰 Кассовые сборы: ").append(movie.getBoxOffice()).append("\n");
            }

            if (movie.getAwards() != null && !movie.getAwards().isEmpty()) {
                details.append("🏆 Награды: ").append(movie.getAwards()).append("\n");
            }

            if (movie.getProduction() != null && !movie.getProduction().isEmpty()) {
                details.append("🏢 Производство: ").append(movie.getProduction()).append("\n");
            }

            if (movie.getImdbId() != null && !movie.getImdbId().isEmpty()) {
                details.append("\n🔗 IMDb ID: ").append(movie.getImdbId()).append("\n");
            }

            // Если есть постер, отправляем фото с описанием
            if (movie.getPoster() != null && !movie.getPoster().isEmpty() && !movie.getPoster().equals("N/A")) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(chatId.toString());
                photo.setPhoto(new InputFile(movie.getPoster()));
                photo.setCaption(details.toString());

                try {
                    execute(photo);
                } catch (TelegramApiException e) {
                    log.warn("Failed to send photo, sending text instead", e);
                    sendMessage(chatId, details.toString());
                }
            } else {
                sendMessage(chatId, details.toString());
            }

        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "❌ Фильм не найден.");
            log.error("Movie not found: {}", movieId, e);
        } catch (Exception e) {
            sendMessage(chatId, "❌ Произошла ошибка при получении информации о фильме.");
            log.error("Error getting movie details", e);
        }
    }

    private void sendSearchResults(Long chatId, String searchQuery) {
        try {
            SearchResponse.MovieSearchResult[] results = movieApiService.searchMoviesList(searchQuery);

            if (results == null || results.length == 0) {
                sendMessage(chatId, "❌ Фильмы по запросу \"" + searchQuery + "\" не найдены.");
                return;
            }

            String messageText = "🔍 Результаты поиска по запросу \"" + searchQuery + "\":\n\nВыберите фильм для просмотра подробной информации:";

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            for (SearchResponse.MovieSearchResult result : results) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                String buttonText = result.getTitle();
                if (result.getYear() != null) {
                    buttonText += " (" + result.getYear() + ")";
                }
                button.setText(buttonText);
                button.setCallbackData("search_" + result.getImdbId());
                row.add(button);
                keyboard.add(row);
            }

            keyboardMarkup.setKeyboard(keyboard);

            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(messageText);
            message.setReplyMarkup(keyboardMarkup);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error sending search results", e);
            }
        } catch (Exception e) {
            log.error("Error searching movies", e);
            sendMessage(chatId, "❌ Произошла ошибка при поиске фильмов. Попробуйте позже.");
        }
    }

    private void sendSearchMovieDetails(Long chatId, String imdbId) {
        try {
            MovieApiResponse movieResponse = movieApiService.getMovieDetails(imdbId);

            if (movieResponse == null) {
                sendMessage(chatId, "❌ Информация о фильме не найдена.");
                return;
            }

            StringBuilder details = new StringBuilder();
            details.append("🎬 ").append(movieResponse.getTitle() != null ? movieResponse.getTitle() : "Неизвестно").append("\n\n");

            if (movieResponse.getYear() != null && !movieResponse.getYear().isEmpty()) {
                details.append("📅 Год выпуска: ").append(movieResponse.getYear()).append("\n");
            }

            if (movieResponse.getReleased() != null && !movieResponse.getReleased().isEmpty()) {
                details.append("📆 Дата выхода: ").append(movieResponse.getReleased()).append("\n");
            }

            if (movieResponse.getRuntime() != null && !movieResponse.getRuntime().isEmpty()) {
                details.append("⏱ Длительность: ").append(movieResponse.getRuntime()).append("\n");
            }

            if (movieResponse.getGenre() != null && !movieResponse.getGenre().isEmpty()) {
                details.append("🎭 Жанры: ").append(movieResponse.getGenre()).append("\n");
            }

            if (movieResponse.getRated() != null && !movieResponse.getRated().isEmpty()) {
                details.append("📊 Рейтинг: ").append(movieResponse.getRated()).append("\n");
            }

            if (movieResponse.getImdbRating() != null && !movieResponse.getImdbRating().isEmpty()) {
                details.append("⭐ IMDb рейтинг: ").append(movieResponse.getImdbRating()).append("\n");
            }

            if (movieResponse.getDirector() != null && !movieResponse.getDirector().isEmpty()) {
                details.append("🎬 Режиссер: ").append(movieResponse.getDirector()).append("\n");
            }

            if (movieResponse.getWriter() != null && !movieResponse.getWriter().isEmpty()) {
                details.append("✍️ Сценарист: ").append(movieResponse.getWriter()).append("\n");
            }

            if (movieResponse.getActors() != null && !movieResponse.getActors().isEmpty()) {
                details.append("👥 Актёры: ").append(movieResponse.getActors()).append("\n");
            }

            if (movieResponse.getPlot() != null && !movieResponse.getPlot().isEmpty()) {
                details.append("\n📖 Описание:\n").append(movieResponse.getPlot()).append("\n");
            }

            if (movieResponse.getLanguage() != null && !movieResponse.getLanguage().isEmpty()) {
                details.append("\n🌐 Язык: ").append(movieResponse.getLanguage()).append("\n");
            }

            if (movieResponse.getCountry() != null && !movieResponse.getCountry().isEmpty()) {
                details.append("🌍 Страна: ").append(movieResponse.getCountry()).append("\n");
            }

            if (movieResponse.getBoxOffice() != null && !movieResponse.getBoxOffice().isEmpty()) {
                details.append("💰 Кассовые сборы: ").append(movieResponse.getBoxOffice()).append("\n");
            }

            if (movieResponse.getAwards() != null && !movieResponse.getAwards().isEmpty()) {
                details.append("🏆 Награды: ").append(movieResponse.getAwards()).append("\n");
            }

            if (movieResponse.getProduction() != null && !movieResponse.getProduction().isEmpty()) {
                details.append("🏢 Производство: ").append(movieResponse.getProduction()).append("\n");
            }

            if (movieResponse.getImdbID() != null && !movieResponse.getImdbID().isEmpty()) {
                details.append("\n🔗 IMDb ID: ").append(movieResponse.getImdbID()).append("\n");
            }

            // Если есть постер, отправляем фото с описанием
            if (movieResponse.getPoster() != null && !movieResponse.getPoster().isEmpty() && !movieResponse.getPoster().equals("N/A")) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(chatId.toString());
                photo.setPhoto(new InputFile(movieResponse.getPoster()));
                photo.setCaption(details.toString());

                try {
                    execute(photo);
                } catch (TelegramApiException e) {
                    log.warn("Failed to send photo, sending text instead", e);
                    sendMessage(chatId, details.toString());
                }
            } else {
                sendMessage(chatId, details.toString());
            }

        } catch (Exception e) {
            log.error("Error getting movie details for IMDb ID: {}", imdbId, e);
            sendMessage(chatId, "❌ Произошла ошибка при получении информации о фильме.");
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
}

