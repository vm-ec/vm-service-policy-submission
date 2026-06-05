package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.dto.BedrockAgentRequest;
import com.vm.service.policysubmission.dto.BedrockAgentResponse;
import com.vm.service.policysubmission.service.BedrockAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/bedrock-agent")
@Slf4j
@Tag(name = "Bedrock Agent", description = "APIs for interacting with AWS Bedrock Agent for FNOL processing")
public class BedrockAgentController {

    @Autowired
    private BedrockAgentService bedrockAgentService;

    @PostMapping("/invoke")
    @Operation(summary = "Invoke Bedrock Agent", description = "Send email data to Bedrock Agent for processing")
    public ResponseEntity<BedrockAgentResponse> invokeAgent(@RequestBody BedrockAgentRequest request) {
        log.info("Received request to invoke Bedrock Agent");
        BedrockAgentResponse response = bedrockAgentService.invokeAgent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details")
    @Operation(summary = "Get Agent Details", description = "Retrieve mock Bedrock Agent configuration and capabilities")
    public ResponseEntity<Map<String, Object>> getAgentDetails() {
        log.info("Fetching Bedrock Agent details");
        Map<String, Object> agentDetails = bedrockAgentService.getMockAgentDetails();
        return ResponseEntity.ok(agentDetails);
    }

    @GetMapping("/health")
    @Operation(summary = "Agent Health Check", description = "Check if Bedrock Agent is accessible")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Bedrock Agent health check requested");
        Map<String, Object> health = new java.util.HashMap<>();
        health.put("status", "UP");
        health.put("service", "Bedrock Agent Service");
        health.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(health);
    }
}

