package com.grootan.policyrouter;

import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.engine.StateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    private StateMachine stateMachine;

    @BeforeEach
    void setup() {
        stateMachine = new StateMachine();
    }

    @Test
    void shouldTransitionFromPendingToRouted() {
        Message message = Message.builder()
                .status(MessageStatus.PENDING)
                .build();

        stateMachine.transition(message, MessageStatus.ROUTED);

        assertEquals(MessageStatus.ROUTED, message.getStatus());
    }

    @Test
    void shouldTransitionFromRoutedToDispatched() {
        Message message = Message.builder()
                .status(MessageStatus.ROUTED)
                .build();

        stateMachine.transition(message, MessageStatus.DISPATCHED);

        assertEquals(MessageStatus.DISPATCHED, message.getStatus());
    }

    @Test
    void shouldTransitionFromDispatchedToSent() {
        Message message = Message.builder()
                .status(MessageStatus.DISPATCHED)
                .build();

        stateMachine.transition(message, MessageStatus.SENT);

        assertEquals(MessageStatus.SENT, message.getStatus());
    }

    @Test
    void shouldTransitionFromFailedToRetrying() {
        Message message = Message.builder()
                .status(MessageStatus.FAILED)
                .build();

        stateMachine.transition(message, MessageStatus.RETRYING);

        assertEquals(MessageStatus.RETRYING, message.getStatus());
    }

    @Test
    void shouldThrowOnInvalidTransition() {
        Message message = Message.builder()
                .status(MessageStatus.SENT)
                .build();

        assertThrows(IllegalStateException.class, () ->
                stateMachine.transition(message, MessageStatus.PENDING)
        );
    }

    @Test
    void shouldThrowWhenTransitioningFromDead() {
        Message message = Message.builder()
                .status(MessageStatus.DEAD)
                .build();

        assertThrows(IllegalStateException.class, () ->
                stateMachine.transition(message, MessageStatus.RETRYING)
        );
    }
}