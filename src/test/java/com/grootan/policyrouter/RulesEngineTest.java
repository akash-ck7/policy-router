package com.grootan.policyrouter;

import com.grootan.policyrouter.domain.enums.MessageType;
import com.grootan.policyrouter.domain.enums.Priority;
import com.grootan.policyrouter.domain.model.Message;
import com.grootan.policyrouter.domain.model.RoutingRule;
import com.grootan.policyrouter.domain.repository.RoutingRuleRepository;
import com.grootan.policyrouter.engine.RulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesEngineTest {

    @Mock
    private RoutingRuleRepository ruleRepository;

    @InjectMocks
    private RulesEngine rulesEngine;

    private RoutingRule criticalRule;
    private RoutingRule promotionRule;

    @BeforeEach
    void setup() {
        criticalRule = new RoutingRule();
        criticalRule.setName("Critical alerts");
        criticalRule.setPriority(100);
        criticalRule.setConditions(Map.of("type", "ALERT", "priority", "CRITICAL"));
        criticalRule.setChannels(new String[]{"sms", "email"});
        criticalRule.setRetryCount(3);
        criticalRule.setActive(true);

        promotionRule = new RoutingRule();
        promotionRule.setName("Promotions");
        promotionRule.setPriority(50);
        promotionRule.setConditions(Map.of("type", "PROMOTION"));
        promotionRule.setChannels(new String[]{"email"});
        promotionRule.setRetryCount(2);
        promotionRule.setActive(true);
    }

    @Test
    void shouldMatchCriticalAlertRule() {
        when(ruleRepository.findByIsActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(criticalRule, promotionRule));

        Message message = Message.builder()
                .type(MessageType.ALERT)
                .priority(Priority.CRITICAL)
                .build();

        RoutingRule result = rulesEngine.evaluate(message);

        assertEquals("Critical alerts", result.getName());
        assertArrayEquals(new String[]{"sms", "email"}, result.getChannels());
    }

    @Test
    void shouldMatchPromotionRule() {
        when(ruleRepository.findByIsActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(criticalRule, promotionRule));

        Message message = Message.builder()
                .type(MessageType.PROMOTION)
                .priority(Priority.LOW)
                .build();

        RoutingRule result = rulesEngine.evaluate(message);

        assertEquals("Promotions", result.getName());
        assertArrayEquals(new String[]{"email"}, result.getChannels());
    }

    @Test
    void shouldReturnDefaultRuleWhenNoMatch() {
        when(ruleRepository.findByIsActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of());

        Message message = Message.builder()
                .type(MessageType.NOTIFICATION)
                .priority(Priority.LOW)
                .build();

        RoutingRule result = rulesEngine.evaluate(message);

        assertEquals("Default fallback", result.getName());
        assertArrayEquals(new String[]{"email"}, result.getChannels());
    }

    @Test
    void shouldReturnHighestPriorityMatchingRule() {
        when(ruleRepository.findByIsActiveTrueOrderByPriorityDesc())
                .thenReturn(List.of(criticalRule, promotionRule));

        Message message = Message.builder()
                .type(MessageType.ALERT)
                .priority(Priority.CRITICAL)
                .build();

        RoutingRule result = rulesEngine.evaluate(message);

        assertEquals(100, result.getPriority());
    }
}