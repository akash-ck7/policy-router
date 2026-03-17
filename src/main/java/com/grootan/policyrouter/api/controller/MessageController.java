package com.grootan.policyrouter.api.controller;

import com.grootan.policyrouter.api.dto.MessageRequest;
import com.grootan.policyrouter.api.dto.MessageResponse;
import com.grootan.policyrouter.api.dto.TrackingResponse;
import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> submit(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.submit(request));
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAll(
            @RequestParam(required = false) MessageStatus status) {
        if (status != null) {
            return ResponseEntity.ok(messageService.getByStatus(status));
        }
        return ResponseEntity.ok(messageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(messageService.getById(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<TrackingResponse> getTracking(@PathVariable UUID id) {
        return ResponseEntity.ok(messageService.getTracking(id));
    }
}