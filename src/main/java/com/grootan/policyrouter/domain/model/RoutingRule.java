package com.grootan.policyrouter.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "routing_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RoutingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> conditions;

    @Column(columnDefinition = "text[]")
    private String[] channels;

    @Column(columnDefinition = "text[]")
    private String[] fallback;

    @Column(name = "retry_count")
    @Builder.Default
    private int retryCount = 3;

    @Column(name = "retry_delay_ms")
    @Builder.Default
    private int retryDelayMs = 5000;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}