package com.vm.service.policysubmission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BedrockAgentRequest {
    private String agentId;
    private String agentAliasId;
    private String sessionId;
    private String inputText;

    // Email details
    private String subject;
    private String body;
    private String sender;
    private Instant timestamp;
    private String queueId;
    private List<String> recipients;
    private List<String> attachmentPaths;

    // Additional session-level context
    private Map<String, String> sessionStateVariables;
}

