package com.grootan.policyrouter.domain.repository;

import com.grootan.policyrouter.domain.model.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, UUID> {
    List<DeliveryLog> findByMessageId(UUID messageId);
}