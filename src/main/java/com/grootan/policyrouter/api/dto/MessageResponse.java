package com.grootan.policyrouter.api.dto;

import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.enums.MessageType;
import com.grootan.policyrouter.domain.enums.Priority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
    private UUID id;
    private MessageType type;
    private Priority priority;
    private MessageStatus status;
    private UUID userId;
    private LocalDateTime createdAt;
}