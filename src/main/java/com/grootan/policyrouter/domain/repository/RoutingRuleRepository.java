package com.grootan.policyrouter.domain.repository;

import com.grootan.policyrouter.domain.model.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {
    List<RoutingRule> findByIsActiveTrueOrderByPriorityDesc();
}