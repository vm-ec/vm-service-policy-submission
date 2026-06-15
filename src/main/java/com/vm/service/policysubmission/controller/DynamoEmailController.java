package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.dto.ContextEmailRequest;
import com.vm.service.policysubmission.dto.DynamoAgentRequest;
import com.vm.service.policysubmission.dto.DynamoAgentResponse;
import com.vm.service.policysubmission.dto.EmailRequest;
import com.vm.service.policysubmission.service.DynamoAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fnol-dynamo")
@Slf4j
@Tag(name = "FNOL Dynamo", description = "APIs for processing FNOL emails through Dynamo agent scoring")
public class DynamoEmailController {

    @Autowired
    private DynamoAgentService dynamoAgentService;

    @PostMapping(path = "/email", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Process FNOL Email", description = "Receive FNOL email and forward to Dynamo agent pipeline for scoring")
    public ResponseEntity<Map<String, Object>> process(@RequestBody ContextEmailRequest contextRequest) {

        if(contextRequest == null || contextRequest.getContext() == null) {
            log.warn("Received null or invalid email request");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Invalid email request payload");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        EmailRequest request = contextRequest.getContext();
        log.info("Received claims FNOL email from: {} with subject: {}", request.getSender(), request.getSubject());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "RECEIVED");
        response.put("subject", request.getSubject());
        response.put("sender", request.getSender());
        response.put("recipients", request.getRecipients());
        response.put("queueId", request.getQueueId());
        response.put("timestamp", request.getTimestamp());
        response.put("bodyLength", request.getBody() != null ? request.getBody().length() : 0);
        response.put("attachmentPaths", request.getAttachmentPaths());
        response.put("message", "Email payload acknowledged");

        try {
            log.info("Forwarding email to Dynamo agent pipeline");

            int claimsNumber = (int) (Math.random() * 1000);
            String formattedClaimsNumber = String.format("%04d", claimsNumber);

            DynamoAgentRequest agentRequest = DynamoAgentRequest.builder()
                    .claimReference(firstNonBlank(request.getMessageId(), request.getQueueId(), "CM-2026-" + formattedClaimsNumber))
                    .claimText(request.getBody())
                    .confidenceThreshold(60)
                    .structuredFields(DynamoAgentRequest.StructuredFields.builder()
                            .cover("UNKNOWN")
                            .incidentType(request.getSubject())
                            .injuryIndicator(Boolean.FALSE)
                            .build())
                    .build();

            DynamoAgentResponse agentResponse = dynamoAgentService.invokeAgent(agentRequest);
            response.put("dynamoAgentResponse", agentResponse);
            response.put("claimReference", agentResponse.getClaimReference());
            response.put("severityIndex", agentResponse.getSeverityIndex());
            response.put("severityBand", agentResponse.getSeverityBand());
            response.put("complexityBand", agentResponse.getComplexityBand());
            response.put("routingDecision", agentResponse.getRoutingDecision());
            response.put("detectedSignals", agentResponse.getDetectedSignals());
            response.put("rationale", agentResponse.getRationale());
            response.put("disclaimer", agentResponse.getDisclaimer());
            response.put("agentStatus", "SUCCESS");

            log.info("Email successfully processed by Dynamo agent with claimReference: {}", agentResponse.getClaimReference());
        } catch (Exception e) {
            log.error("Error forwarding email to Dynamo agent", e);
            response.put("agentStatus", "ERROR");
            response.put("agentError", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
