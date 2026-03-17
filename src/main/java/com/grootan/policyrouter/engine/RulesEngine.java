package com.grootan.policyrouter.engine;

import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.domain.model.RoutingRule;
import com.grootan.policyrouter.domain.repository.RoutingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RulesEngine {

    private final RoutingRuleRepository ruleRepository;

    public RoutingRule evaluate(Message message) {
        List<RoutingRule> rules = ruleRepository.findByIsActiveTrueOrderByPriorityDesc();

        log.info("Evaluating {} rules for message id={} type={} priority={}",
                rules.size(), message.getId(), message.getType(), message.getPriority());

        for (RoutingRule rule : rules) {
            if (matches(rule, message)) {
                log.info("Rule matched: '{}' for message id={}", rule.getName(), message.getId());
                return rule;
            }
        }

        log.warn("No rule matched for message id={} — using default email fallback", message.getId());
        return buildDefaultRule();
    }

    private boolean matches(RoutingRule rule, Message message) {
        Map<String, String> conditions = rule.getConditions();
        if (conditions == null || conditions.isEmpty()) return true;

        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String expected = entry.getValue().toUpperCase();

            boolean matched = switch (key) {
                case "type"     -> message.getType().name().equals(expected);
                case "priority" -> message.getPriority().name().equals(expected);
                default         -> true;
            };

            if (!matched) return false;
        }
        return true;
    }

    private RoutingRule buildDefaultRule() {
        RoutingRule rule = new RoutingRule();
        rule.setName("Default fallback");
        rule.setChannels(new String[]{"email"});
        rule.setFallback(new String[]{});
        rule.setRetryCount(2);
        rule.setRetryDelayMs(5000);
        return rule;
    }
}