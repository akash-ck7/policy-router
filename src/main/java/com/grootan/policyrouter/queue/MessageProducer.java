package com.grootan.policyrouter.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.queue.name}")
    private String queueName;

    public void enqueue(UUID messageId) {
        try {
            String id = messageId.toString();
            redisTemplate.opsForList().leftPush(queueName, id);
            log.info("Message id={} enqueued to Redis queue '{}'", id, queueName);
        } catch (Exception e) {
            log.error("Failed to enqueue message id={}: {}", messageId, e.getMessage());
            throw new RuntimeException("Failed to enqueue message", e);
        }
    }
}