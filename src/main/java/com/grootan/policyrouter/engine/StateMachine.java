package com.grootan.policyrouter.engine;

import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StateMachine {

    public void transition(Message message, MessageStatus newStatus) {
        MessageStatus current = message.getStatus();

        if (!isValidTransition(current, newStatus)) {
            throw new IllegalStateException(
                    "Invalid transition from " + current + " to " + newStatus
                            + " for message id=" + message.getId()
            );
        }

        log.info("Message id={} transitioning {} -> {}", message.getId(), current, newStatus);
        message.setStatus(newStatus);
    }

    private boolean isValidTransition(MessageStatus from, MessageStatus to) {
        return switch (from) {
            case PENDING    -> to == MessageStatus.ROUTED || to == MessageStatus.FAILED;
            case ROUTED     -> to == MessageStatus.DISPATCHED || to == MessageStatus.FAILED;
            case DISPATCHED -> to == MessageStatus.SENT || to == MessageStatus.FAILED;
            case FAILED     -> to == MessageStatus.RETRYING || to == MessageStatus.DEAD;
            case RETRYING   -> to == MessageStatus.DISPATCHED || to == MessageStatus.DEAD;
            case SENT, DEAD -> false;
        };
    }
}