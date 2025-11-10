# Film Description Service

## Описание проекта

Spring Boot приложение для поиска информации о фильмах через внешний API (Free Movie Series DB API) и сохранения понравившихся фильмов в локальную H2 базу данных.

## Технологический стек

- **Java 21**
- **Spring Boot 4.0.0-SNAPSHOT**
- **Spring Web MVC** - для создания REST API и обслуживания веб-страниц
- **Spring Data JPA** - для работы с базой данных
- **Thymeleaf** - для серверного рендеринга HTML страниц
- **H2 Database** - in-memory база данных для хранения информации о фильмах
- **Lombok** - для уменьшения boilerplate кода
- **Free Movie Series DB API** - внешний API для получения информации о фильмах

## Архитектура проекта

Проект следует принципам чистой архитектуры и разделения ответственности:

```
src/main/java/by/ilyushenko/film/
├── config/           # Конфигурация приложения
├── controller/       # REST API и Web контроллеры
├── dto/             # Data Transfer Objects
├── exception/       # Обработка исключений
├── model/           # JPA сущности
├── repository/      # JPA репозитории
└── service/         # Бизнес-логика (интерфейсы и реализации)
```

### Структура пакетов

#### 1. **config** - Конфигурация
- `AppConfig.java` - настройка Spring бинов (RestTemplate для HTTP запросов к внешнему API)

#### 2. **controller** - Контроллеры
- `MovieController.java` - REST API для работы с фильмами:
  - `GET /api/movies/search-list` - поиск списка фильмов по названию
  - `GET /api/movies/details` - получение детальной информации о фильме
  - `POST /api/movies/save` - сохранение фильма в базу данных
  - `GET /api/movies/saved` - получение всех сохраненных фильмов
  - `GET /api/movies/saved/{id}` - получение фильма по ID
  - `DELETE /api/movies/{id}` - удаление фильма из базы данных
- `WebController.java` - контроллер для веб-страниц (Thymeleaf templates)

#### 3. **dto** - Data Transfer Objects
- `MovieApiResponse.java` - DTO для ответа от API с детальной информацией о фильме
- `SearchResponse.java` - DTO для ответа от API при поиске списка фильмов
- `MovieSearchResultResponse.java` - DTO для нормализованного ответа результатов поиска
- `DetailedMovieResponse.java` - DTO для детального ответа от API
- `MovieSearchRequest.java` - DTO для запроса поиска фильма

#### 4. **exception** - Обработка исключений
- `GlobalExceptionHandler.java` - глобальный обработчик исключений с аннотацией `@ControllerAdvice`

#### 5. **model** - JPA сущности
- `Movie.java` - основная JPA сущность для представления фильма в базе данных
  - Поля: id, imdbId, title, releaseYear, rating, plot, director, actors, genres, poster, released, runtime, imdbRating, boxOffice, production, writer, language, country, awards, savedAt

#### 6. **repository** - JPA репозитории
- `MovieRepository.java` - интерфейс JPA репозитория для работы с фильмами, расширяет `JpaRepository<Movie, Long>`

#### 7. **service** - Бизнес-логика

**Интерфейсы:**
- `MovieApiService.java` - интерфейс для работы с внешним API
- `MovieService.java` - интерфейс для бизнес-логики работы с фильмами в базе данных

**Реализации:**
- `MovieApiServiceImpl.java` - реализация работы с внешним Free Movie Series DB API
  - Методы поиска: `searchByTitle()`, `searchByTitleAndYear()`, `searchByImdbId()`
  - Методы получения списка: `searchMoviesList()` - возвращает краткую информацию
  - Методы получения деталей: `getMovieDetails()` - возвращает полную информацию
  - Преобразование: `convertToMovie()` - конвертирует DTO в JPA сущность
- `MovieServiceImpl.java` - реализация бизнес-логики работы с базой данных
  - CRUD операции для сохраненных фильмов
  - Проверка существования фильма в базе

### Преимущества архитектуры

1. **Разделение ответственности**: каждый пакет отвечает за свою область
2. **Инверсия зависимостей**: использование интерфейсов для сервисов
3. **SOLID принципы**: особенно Single Responsibility и Dependency Inversion
4. **Тестируемость**: легко создавать моки для интерфейсов
5. **Расширяемость**: легко добавлять новые функции и менять реализацию

## API Endpoints

### Поиск фильмов

#### GET `/api/movies/search-list?title={title}`
Получить список фильмов по названию (краткая информация)
- **Параметры**: `title` - название фильма
- **Ответ**: массив объектов с полями: title, year, imdbId, rank, actors, aka, poster

#### GET `/api/movies/details?imdbId={id}`
Получить детальную информацию о фильме по IMDb ID
- **Параметры**: `imdbId` - ID фильма на IMDb
- **Ответ**: объект с полной информацией о фильме

### Работа с сохраненными фильмами

#### POST `/api/movies/save`
Сохранить фильм в базу данных
- **Тело запроса**: JSON объект MovieApiResponse
- **Ответ**: сохраненный фильм с присвоенным ID

#### GET `/api/movies/saved`
Получить все сохраненные фильмы
- **Ответ**: массив всех сохраненных фильмов

#### GET `/api/movies/saved/{id}`
Получить фильм по ID
- **Параметры**: `id` - идентификатор фильма
- **Ответ**: объект фильма

#### DELETE `/api/movies/{id}`
Удалить фильм из базы данных
- **Параметры**: `id` - идентификатор фильма
- **Ответ**: HTTP 204 No Content

## Веб-интерфейс

### Главная страница (`/`)
- Поиск фильмов по названию
- Отображение списка результатов поиска
- Просмотр детальной информации о фильме
- Сохранение фильма в базу данных

### Страница сохраненных фильмов (`/saved`)
- Просмотр всех сохраненных фильмов
- Удаление фильмов из базы данных
- Отображение детальной информации о каждом фильме

## База данных

**H2 In-Memory Database**
- Тип: in-memory (данные исчезают после остановки приложения)
- Console: доступна по адресу `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:moviesdb`
- User: `sa`
- Password: (пустой)

### Таблица `movies`

| Колонка | Тип | Описание |
|---------|-----|----------|
| id | BIGINT | Первичный ключ |
| imdb_id | VARCHAR(20) | IMDb ID (уникальный) |
| title | VARCHAR(255) | Название фильма |
| release_year | INTEGER | Год выпуска |
| rating | VARCHAR(255) | Рейтинг |
| plot | VARCHAR(1000) | Описание |
| director | VARCHAR(255) | Режиссер |
| actors | VARCHAR(1000) | Актеры |
| genres | VARCHAR(255) | Жанры |
| poster | VARCHAR(255) | URL постера |
| released | VARCHAR(255) | Дата выпуска |
| runtime | VARCHAR(255) | Длительность |
| imdb_rating | VARCHAR(255) | IMDb рейтинг |
| box_office | VARCHAR(255) | Кассовые сборы |
| production | VARCHAR(255) | Продюсер |
| writer | VARCHAR(255) | Сценарист |
| language | VARCHAR(255) | Язык |
| country | VARCHAR(255) | Страна |
| awards | VARCHAR(255) | Награды |
| saved_at | TIMESTAMP | Дата/время сохранения |

## Запуск приложения

1. Убедитесь, что Java 21 установлена
2. Запустите приложение командой:
```bash
./gradlew bootRun
```

3. Откройте в браузере:
- http://localhost:8080 - главная страница
- http://localhost:8080/h2-console - консоль базы данных

## Внешний API

Проект использует Free Movie Series DB API:
- **Base URL**: `https://imdb.iamidiotareyoutoo.com/search`
- **Поиск**: `?q={query}` - возвращает список фильмов
- **Детали**: `?tt={imdbId}` - возвращает детальную информацию

## Конфигурация

Основные настройки в `application.properties`:

```properties
# Порт сервера
server.port=8080

# H2 Database
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:moviesdb
spring.jpa.hibernate.ddl-auto=update

# Внешний API
movie.api.base-url=https://imdb.iamidiotareyoutoo.com/search

# JSON форматирование
spring.jackson.serialization.indent-output=true
```

## Особенности реализации

1. **Логирование**: все основные операции логируются через SLF4J
2. **Обработка ошибок**: глобальный обработчик исключений
3. **Валидация**: проверка дубликатов при сохранении фильмов
4. **Async операции**: загрузка данных из API происходит асинхронно
5. **UI/UX**: отображение индикатора загрузки во время поиска

## Разработка

### Добавление нового функционала

1. Добавьте новый эндпоинт в `MovieController`
2. Если нужна бизнес-логика - добавьте метод в `MovieService`
3. Для работы с внешним API - используйте `MovieApiService`
4. Для работы с БД - используйте `MovieRepository`

### Тестирование

```bash
./gradlew test
```

## Лицензия

Это учебный проект для демонстрации использования Spring Boot.
