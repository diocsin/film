# Multi-stage build для film-description-service
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Копируем файлы Gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN gradle bootJar --no-daemon

# Финальный образ
FROM eclipse-temurin:21-jre
WORKDIR /app

# Копируем JAR из stage сборки
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

