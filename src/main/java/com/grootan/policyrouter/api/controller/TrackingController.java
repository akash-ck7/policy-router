package com.grootan.policyrouter.api.controller;

import com.grootan.policyrouter.domain.model.DeadLetterEntry;
import com.grootan.policyrouter.domain.repository.DeadLetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dlq")
@RequiredArgsConstructor
public class TrackingController {

    private final DeadLetterRepository deadLetterRepository;

    @GetMapping
    public ResponseEntity<List<DeadLetterEntry>> getDlq() {
        return ResponseEntity.ok(deadLetterRepository.findAll());
    }
}