package com.grootan.policyrouter.api.dto;

import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.enums.MessageType;
import com.grootan.policyrouter.domain.enums.Priority;
import com.grootan.policyrouter.domain.model.DeliveryLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TrackingResponse {
    private UUID messageId;
    private MessageStatus status;
    private MessageType type;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeliveryLog> deliveryLogs;
}