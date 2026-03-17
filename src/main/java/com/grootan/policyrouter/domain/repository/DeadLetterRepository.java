package com.grootan.policyrouter.domain.repository;

import com.grootan.policyrouter.domain.model.DeadLetterEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DeadLetterRepository extends JpaRepository<DeadLetterEntry, UUID> {
    List<DeadLetterEntry> findByMessageId(UUID messageId);
}