package com.grootan.policyrouter.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class RuleRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotNull(message = "priority is required")
    private Integer priority;

    @NotNull(message = "conditions is required")
    private Map<String, String> conditions;

    @NotNull(message = "channels is required")
    private String[] channels;

    private String[] fallback;

    private Integer retryCount = 3;

    private Integer retryDelayMs = 5000;
}