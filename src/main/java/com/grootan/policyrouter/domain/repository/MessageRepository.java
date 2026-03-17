package com.grootan.policyrouter.domain.repository;

import com.grootan.policyrouter.domain.enums.MessageStatus;
import com.grootan.policyrouter.domain.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByStatus(MessageStatus status);
    List<Message> findByUserId(UUID userId);
}