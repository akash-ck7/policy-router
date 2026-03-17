package com.grootan.policyrouter.api.dto;

import com.grootan.policyrouter.domain.enums.MessageType;
import com.grootan.policyrouter.domain.enums.Priority;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class MessageRequest {

    @NotNull(message = "type is required")
    private MessageType type;

    @NotNull(message = "priority is required")
    private Priority priority;

    @NotNull(message = "userId is required")
    private UUID userId;

    private Map<String, Object> payload;
}