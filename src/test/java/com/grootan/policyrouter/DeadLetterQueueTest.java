package com.grootan.policyrouter;

import com.grootan.policyrouter.channel.ChannelRegistry;
import com.grootan.policyrouter.channel.MessageChannel;
import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.enums.MessageType;
import com.grootan.policyrouter.domain.enums.Priority;
import com.grootan.policyrouter.domain.model.DeadLetterEntry;
import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.domain.model.RoutingRule;
import com.grootan.policyrouter.domain.repository.DeadLetterRepository;
import com.grootan.policyrouter.domain.repository.DeliveryLogRepository;
import com.grootan.policyrouter.domain.repository.MessageRepository;
import com.grootan.policyrouter.engine.RoutingEngine;
import com.grootan.policyrouter.engine.RulesEngine;
import com.grootan.policyrouter.engine.StateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueTest {

    @Mock
    private RulesEngine rulesEngine;

    @Mock
    private ChannelRegistry channelRegistry;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Mock
    private DeadLetterRepository deadLetterRepository;

    private RoutingEngine routingEngine;
    private StateMachine stateMachine;
    private RoutingRule rule;

    @BeforeEach
    void setup() {
        stateMachine = new StateMachine();
        routingEngine = new RoutingEngine(
                rulesEngine,
                stateMachine,
                channelRegistry,
                messageRepository,
                deliveryLogRepository,
                deadLetterRepository
        );

        rule = new RoutingRule();
        rule.setName("Test rule");
        rule.setChannels(new String[]{"email"});
        rule.setFallback(new String[]{});
        rule.setRetryCount(0);
        rule.setRetryDelayMs(0);
        rule.setActive(true);
    }

    private Message buildMessage() {
        return Message.builder()
                .id(UUID.randomUUID())
                .type(MessageType.ALERT)
                .priority(Priority.CRITICAL)
                .status(MessageStatus.PENDING)
                .userId(UUID.randomUUID())
                .build();
    }

    @Test
    void shouldMoveMessageToDlqWhenChannelAlwaysFails() throws Exception {
        Message message = buildMessage();
        MessageChannel failingChannel = mock(MessageChannel.class);
        doThrow(new Exception("Provider down")).when(failingChannel).send(any());

        when(rulesEngine.evaluate(any())).thenReturn(rule);
        when(channelRegistry.resolve("email")).thenReturn(failingChannel);
        when(messageRepository.save(any())).thenReturn(message);

        routingEngine.route(message);

        verify(deadLetterRepository, times(1)).save(any(DeadLetterEntry.class));
        assertEquals(MessageStatus.DEAD, message.getStatus());
    }

    @Test
    void shouldNotMoveToDlqWhenChannelSucceeds() throws Exception {
        Message message = buildMessage();
        MessageChannel successChannel = mock(MessageChannel.class);
        doNothing().when(successChannel).send(any());

        when(rulesEngine.evaluate(any())).thenReturn(rule);
        when(channelRegistry.resolve("email")).thenReturn(successChannel);
        when(messageRepository.save(any())).thenReturn(message);

        routingEngine.route(message);

        verify(deadLetterRepository, never()).save(any(DeadLetterEntry.class));
        assertEquals(MessageStatus.SENT, message.getStatus());
    }

    @Test
    void shouldRetryBeforeMovingToDlq() throws Exception {
        Message message = buildMessage();
        MessageChannel failingChannel = mock(MessageChannel.class);
        doThrow(new Exception("Provider down")).when(failingChannel).send(any());

        rule.setRetryCount(2);

        when(rulesEngine.evaluate(any())).thenReturn(rule);
        when(channelRegistry.resolve("email")).thenReturn(failingChannel);
        when(messageRepository.save(any())).thenReturn(message);

        routingEngine.route(message);

        verify(failingChannel, times(3)).send(any());
        verify(deadLetterRepository, times(1)).save(any(DeadLetterEntry.class));
        assertEquals(MessageStatus.DEAD, message.getStatus());
    }

    @Test
    void shouldUseFallbackChannelWhenPrimaryFails() throws Exception {
        Message message = buildMessage();

        rule.setChannels(new String[]{"sms"});
        rule.setFallback(new String[]{"email"});
        rule.setRetryCount(0);

        MessageChannel failingSms = mock(MessageChannel.class);
        doThrow(new Exception("SMS down")).when(failingSms).send(any());

        MessageChannel successEmail = mock(MessageChannel.class);
        doNothing().when(successEmail).send(any());

        when(rulesEngine.evaluate(any())).thenReturn(rule);
        when(channelRegistry.resolve("sms")).thenReturn(failingSms);
        when(channelRegistry.resolve("email")).thenReturn(successEmail);
        when(messageRepository.save(any())).thenReturn(message);

        routingEngine.route(message);

        verify(failingSms, times(1)).send(any());
        verify(successEmail, times(1)).send(any());
    }
}