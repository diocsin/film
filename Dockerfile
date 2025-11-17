# ====================== BUILD STAGE ======================
FROM eclipse-temurin:23-jdk-alpine AS build
# alpine сильно легче и быстрее, и на 2025 год уже полностью поддерживает JDK 23

WORKDIR /app

# 1. Копируем только то, что нужно для разрешения зависимостей
COPY gradle ./gradle
COPY gradlew gradlew
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle

# 2. Скачиваем зависимости (это самый долгий шаг, его хочется закешировать)
#    Важно: делаем это ДО копирования src, чтобы кеш не ломался при изменении кода
RUN chmod +x gradlew && \
    ./gradlew --no-daemon --info dependencies bootJar --dry-run || true
# --dry-run заставит Gradle только разрешить зависимости и скачать всё, но не собирать

# 3. Теперь копируем исходный код
COPY src ./src

# 4. Собираем fat-jar
RUN ./gradlew --no-daemon bootJar

# ====================== RUNTIME STAGE ======================
FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

# Копируем только готовый jar (обычно один)
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar
# если у вас не SNAPSHOT, а конкретная версия, можно *.jar

EXPOSE 8080

# Лучше явно задать разумные JVM опции для контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]