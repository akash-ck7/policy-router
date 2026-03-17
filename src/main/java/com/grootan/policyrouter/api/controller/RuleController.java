package com.grootan.policyrouter.api.controller;

import com.grootan.policyrouter.api.dto.RuleRequest;
import com.grootan.policyrouter.domain.model.RoutingRule;
import com.grootan.policyrouter.domain.repository.RoutingRuleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RoutingRuleRepository ruleRepository;

    @GetMapping
    public ResponseEntity<List<RoutingRule>> getAll() {
        return ResponseEntity.ok(ruleRepository.findByIsActiveTrueOrderByPriorityDesc());
    }

    @PostMapping
    public ResponseEntity<RoutingRule> create(@Valid @RequestBody RuleRequest request) {
        RoutingRule rule = RoutingRule.builder()
                .name(request.getName())
                .priority(request.getPriority())
                .conditions(request.getConditions())
                .channels(request.getChannels())
                .fallback(request.getFallback())
                .retryCount(request.getRetryCount())
                .retryDelayMs(request.getRetryDelayMs())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(ruleRepository.save(rule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutingRule> update(@PathVariable UUID id,
                                              @Valid @RequestBody RuleRequest request) {
        RoutingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        rule.setName(request.getName());
        rule.setPriority(request.getPriority());
        rule.setConditions(request.getConditions());
        rule.setChannels(request.getChannels());
        rule.setFallback(request.getFallback());
        rule.setRetryCount(request.getRetryCount());
        rule.setRetryDelayMs(request.getRetryDelayMs());
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        RoutingRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        rule.setActive(false);
        ruleRepository.save(rule);
        return ResponseEntity.noContent().build();
    }
}