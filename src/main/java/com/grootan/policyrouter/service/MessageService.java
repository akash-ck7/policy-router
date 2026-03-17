package com.grootan.policyrouter.service;

import com.grootan.policyrouter.api.dto.MessageRequest;
import com.grootan.policyrouter.api.dto.MessageResponse;
import com.grootan.policyrouter.api.dto.TrackingResponse;
import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.domain.repository.DeadLetterRepository;
import com.grootan.policyrouter.domain.repository.DeliveryLogRepository;
import com.grootan.policyrouter.domain.repository.MessageRepository;
import com.grootan.policyrouter.queue.MessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeadLetterRepository deadLetterRepository;
    private final MessageProducer messageProducer;

    @Transactional
    public MessageResponse submit(MessageRequest request) {
        Message message = Message.builder()
                .type(request.getType())
                .priority(request.getPriority())
                .payload(request.getPayload())
                .userId(request.getUserId())
                .status(MessageStatus.PENDING)
                .build();

        message = messageRepository.save(message);
        messageProducer.enqueue(message.getId());

        log.info("Message submitted id={} type={}", message.getId(), message.getType());

        return toResponse(message);
    }

    public MessageResponse getById(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found: " + id));
        return toResponse(message);
    }

    public List<MessageResponse> getAll() {
        return messageRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getByStatus(MessageStatus status) {
        return messageRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TrackingResponse getTracking(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found: " + id));

        return TrackingResponse.builder()
                .messageId(message.getId())
                .status(message.getStatus())
                .type(message.getType())
                .priority(message.getPriority())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .deliveryLogs(deliveryLogRepository.findByMessageId(id))
                .build();
    }

    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .type(message.getType())
                .priority(message.getPriority())
                .status(message.getStatus())
                .userId(message.getUserId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}