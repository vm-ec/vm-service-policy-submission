package com.vm.service.policysubmission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BedrockAgentResponse {
    private boolean success;
    private String message;
    private String sessionId;
    private String agentResponse;
    private int statusCode;
    private Map<String, Object> metadata;
    private List<String> traceEvents;
    private long processingTimeMs;
}

