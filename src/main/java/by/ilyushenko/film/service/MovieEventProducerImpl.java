package by.ilyushenko.film.service;

import by.ilyushenko.film.config.RabbitMQConfig;
import by.ilyushenko.film.dto.MovieEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieEventProducerImpl implements MovieEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendMovieSavedEvent(MovieEvent event) {
        try {
            log.info("Sending movie saved event: {}", event.getTitle());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );
            log.info("Movie saved event sent successfully for movie: {}", event.getTitle());
        } catch (Exception e) {
            log.error("Failed to send movie saved event for movie: {}", event.getTitle(), e);
            throw new RuntimeException("Failed to send movie event", e);
        }
    }
}

