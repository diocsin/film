# Инструкция по запуску RabbitMQ и сервисов в Docker

## Способ 1: Запуск всех сервисов через Docker Compose (рекомендуется)

### Шаг 1: Запуск всех сервисов (RabbitMQ + приложение)
```bash
docker-compose up -d
```

Эта команда запустит:
- **RabbitMQ** на портах 5672 (AMQP) и 15672 (веб-интерфейс)
- **film-description-service** на порту 8080

### Шаг 2: Проверка статуса всех сервисов
```bash
docker-compose ps
```

### Шаг 3: Просмотр логов
```bash
# Все сервисы
docker-compose logs -f

# Конкретный сервис
docker-compose logs -f film-description-service
docker-compose logs -f rabbitmq
```

### Шаг 4: Пересборка и перезапуск
```bash
# Пересобрать образы и перезапустить
docker-compose up -d --build

# Пересобрать только один сервис
docker-compose up -d --build film-description-service
```

### Шаг 5: Остановка всех сервисов
```bash
docker-compose down
```

### Шаг 6: Остановка с удалением данных
```bash
docker-compose down -v
```

## Способ 2: Запуск только RabbitMQ (для локальной разработки)

Если нужно запустить только RabbitMQ, а приложение запускать локально:

```bash
docker-compose up -d rabbitmq
```

Или используя Docker напрямую:

```bash
docker run -d \
  --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

### Остановка контейнера
```bash
docker stop rabbitmq
docker rm rabbitmq
```

## Доступ к веб-интерфейсу управления

После запуска RabbitMQ доступен веб-интерфейс управления:

- **URL**: http://localhost:15672
- **Логин**: guest
- **Пароль**: guest

В веб-интерфейсе вы можете:
- Просматривать очереди и сообщения
- Мониторить производительность
- Управлять exchanges и bindings
- Просматривать логи

## Проверка подключения

После запуска RabbitMQ убедитесь, что сервис может подключиться:

1. Проверьте, что RabbitMQ запущен:
   ```bash
   docker ps | grep rabbitmq
   ```

2. Проверьте логи сервиса (film-description-service) - не должно быть ошибок подключения

## Порты

- **5672** - AMQP порт (используется приложениями)
- **15672** - Веб-интерфейс управления RabbitMQ
- **8080** - film-description-service (REST API и веб-интерфейс)

## Настройки в application.properties

Сервис настроен на подключение к RabbitMQ через переменную окружения `RABBITMQ_HOST`:
- В Docker Compose используется имя сервиса `rabbitmq`
- Для локального запуска используется `localhost` (по умолчанию)

```properties
spring.rabbitmq.host=${RABBITMQ_HOST:localhost}
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Порядок запуска для демонстрации

### Вариант 1: Все в Docker (рекомендуется для демонстрации)

1. Запустите все сервисы одной командой:
   ```bash
   docker-compose up -d --build
   ```

2. Дождитесь запуска всех сервисов (проверьте логи):
   ```bash
   docker-compose logs -f
   ```

3. Откройте в браузере:
   - http://localhost:8080 - сервис (веб-интерфейс)
   - http://localhost:15672 - RabbitMQ Management (логин: guest, пароль: guest)

4. Сохраните фильм через сервис - событие будет отправлено в очередь RabbitMQ

### Вариант 2: RabbitMQ в Docker, приложение локально

1. Запустите только RabbitMQ:
   ```bash
   docker-compose up -d rabbitmq
   ```

2. Запустите приложение:
   ```bash
   ./gradlew bootRun
   ```

3. Сохраните фильм через сервис - событие будет отправлено в очередь RabbitMQ

## Устранение проблем

### RabbitMQ не запускается
- Убедитесь, что порты 5672 и 15672 не заняты другими приложениями
- Проверьте логи: `docker-compose logs rabbitmq`

### Ошибка подключения в приложении
- Убедитесь, что RabbitMQ запущен: `docker ps` или `docker-compose ps`
- Проверьте логи сервиса: `docker-compose logs film-description-service`
- В Docker Compose используется хост `rabbitmq`, для локального запуска - `localhost`
- Проверьте, что все сервисы в одной сети Docker (если запущены через docker-compose)

### Сервис не собирается
- Убедитесь, что у вас установлен Docker и Docker Compose
- Проверьте логи сборки: `docker-compose up --build`
- Убедитесь, что порты 8080, 5672, 15672 не заняты другими приложениями

### Очистка данных
Если нужно начать с чистого листа:
```bash
docker-compose down -v
docker-compose up -d
```

