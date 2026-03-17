package com.grootan.policyrouter.queue;

import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.domain.repository.MessageRepository;
import com.grootan.policyrouter.engine.RoutingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWorker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository messageRepository;
    private final RoutingEngine routingEngine;

    @Value("${app.queue.name}")
    private String queueName;

    @Scheduled(fixedDelayString = "${app.queue.worker-delay}")
    public void process() {
        try {
            Object raw = redisTemplate.opsForList().rightPop(queueName);
            if (raw == null) return;

            String messageId = raw.toString();
            log.info("Worker picked up message id={}", messageId);

            Optional<Message> optional = messageRepository.findById(UUID.fromString(messageId));
            if (optional.isEmpty()) {
                log.warn("Message id={} not found in DB, skipping", messageId);
                return;
            }

            Message message = optional.get();

            if (message.getStatus() != MessageStatus.PENDING) {
                log.warn("Message id={} is not PENDING (status={}), skipping",
                        messageId, message.getStatus());
                return;
            }

            routingEngine.route(message);

        } catch (Exception e) {
            log.error("Worker error: {}", e.getMessage(), e);
        }
    }
}