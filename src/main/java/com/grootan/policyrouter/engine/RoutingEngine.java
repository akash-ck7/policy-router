package com.grootan.policyrouter.engine;

import com.grootan.policyrouter.channel.ChannelRegistry;
import com.grootan.policyrouter.channel.MessageChannel;
import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.model.DeadLetterEntry;
import com.grootan.policyrouter.domain.model.DeliveryLog;
import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.domain.model.RoutingRule;
import com.grootan.policyrouter.domain.repository.DeadLetterRepository;
import com.grootan.policyrouter.domain.repository.DeliveryLogRepository;
import com.grootan.policyrouter.domain.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingEngine {

    private final RulesEngine rulesEngine;
    private final StateMachine stateMachine;
    private final ChannelRegistry channelRegistry;
    private final MessageRepository messageRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeadLetterRepository deadLetterRepository;

    public void route(Message message) {
        try {
            RoutingRule rule = rulesEngine.evaluate(message);
            stateMachine.transition(message, MessageStatus.ROUTED);
            messageRepository.save(message);

            List<String> channels = Arrays.asList(rule.getChannels());
            boolean anySuccess = false;

            for (String channelName : channels) {
                boolean success = dispatch(message, channelName, rule);
                if (success) {
                    anySuccess = true;
                    break;
                }
            }

            if (!anySuccess && rule.getFallback() != null
                    && rule.getFallback().length > 0) {
                log.warn("Primary channels failed, trying fallback for message id={}",
                        message.getId());

                message.setStatus(MessageStatus.ROUTED);
                messageRepository.save(message);

                for (String fallback : rule.getFallback()) {
                    boolean success = dispatch(message, fallback, rule);
                    if (success) break;
                }
            }

        } catch (Exception e) {
            log.error("Routing failed for message id={}: {}",
                    message.getId(), e.getMessage());
            if (message.getStatus() == MessageStatus.PENDING ||
                    message.getStatus() == MessageStatus.ROUTED) {
                stateMachine.transition(message, MessageStatus.FAILED);
                messageRepository.save(message);
            }
        }
    }

    private boolean dispatch(Message message, String channelName, RoutingRule rule) {
        int maxRetries = rule.getRetryCount();
        int attempt = 0;

        while (attempt <= maxRetries) {
            try {
                message.setStatus(MessageStatus.ROUTED);
                stateMachine.transition(message, MessageStatus.DISPATCHED);
                messageRepository.save(message);

                MessageChannel channel = channelRegistry.resolve(channelName);
                channel.send(message);

                stateMachine.transition(message, MessageStatus.SENT);
                messageRepository.save(message);

                logDelivery(message, channelName, "SENT", attempt + 1, null, null);
                return true;

            } catch (Exception e) {
                attempt++;
                log.warn("Channel {} failed for message id={} attempt={}: {}",
                        channelName, message.getId(), attempt, e.getMessage());

                logDelivery(message, channelName, "FAILED", attempt, null, e.getMessage());

                if (attempt <= maxRetries) {
                    try {
                        message.setStatus(MessageStatus.DISPATCHED);
                        stateMachine.transition(message, MessageStatus.FAILED);
                        messageRepository.save(message);
                        stateMachine.transition(message, MessageStatus.RETRYING);
                        messageRepository.save(message);
                        if (rule.getRetryDelayMs() > 0) {
                            Thread.sleep(rule.getRetryDelayMs());
                        }
                    } catch (Exception retryEx) {
                        log.error("Retry interrupted: {}", retryEx.getMessage());
                    }
                } else {
                    moveToDlq(message, channelName, e.getMessage());
                }
            }
        }
        return false;
    }

    private void logDelivery(Message message, String channel,
                             String status, int attempt,
                             String providerRef, String error) {
        DeliveryLog deliveryLog = DeliveryLog.builder()
                .message(message)
                .channel(channel)
                .status(status)
                .attempt(attempt)
                .providerRef(providerRef)
                .error(error)
                .sentAt(status.equals("SENT") ? LocalDateTime.now() : null)
                .build();
        deliveryLogRepository.save(deliveryLog);
    }

    private void moveToDlq(Message message, String channel, String reason) {
        log.error("Moving message id={} to DLQ channel={} reason={}",
                message.getId(), channel, reason);

        message.setStatus(MessageStatus.FAILED);
        stateMachine.transition(message, MessageStatus.DEAD);
        messageRepository.save(message);

        DeadLetterEntry entry = DeadLetterEntry.builder()
                .messageId(message.getId())
                .channel(channel)
                .reason(reason)
                .payload(message.getPayload())
                .build();
        deadLetterRepository.save(entry);
    }
}