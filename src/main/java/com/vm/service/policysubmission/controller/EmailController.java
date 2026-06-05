package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.dto.BedrockAgentRequest;
import com.vm.service.policysubmission.dto.BedrockAgentResponse;
import com.vm.service.policysubmission.dto.EmailRequest;
import com.vm.service.policysubmission.service.BedrockAgentService;
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
@RequestMapping("/fnol")
@Slf4j
@Tag(name = "FNOL Email", description = "APIs for processing First Notice of Loss emails")
public class EmailController {

    @Autowired
    private BedrockAgentService bedrockAgentService;

    @PostMapping(path = "/email", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Process FNOL Email", description = "Receive FNOL email with details and attachments, forward to Bedrock Agent for processing")
    public ResponseEntity<Map<String, Object>> process(@RequestBody EmailRequest request) {
        log.info("Received FNOL email from: {} with subject: {}", request.getSender(), request.getSubject());

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

        // Forward email data to Bedrock Agent for processing
        try {
            log.info("Forwarding email to Bedrock Agent for processing");
            BedrockAgentRequest agentRequest = BedrockAgentRequest.builder()
                    .subject(request.getSubject())
                    .body(request.getBody())
                    .sender(request.getSender())
                    .timestamp(request.getTimestamp())
                    .queueId(request.getQueueId())
                    .recipients(request.getRecipients())
                    .attachmentPaths(request.getAttachmentPaths())
                    .inputText(String.format("Process FNOL email from %s with subject: %s",
                        request.getSender(), request.getSubject()))
                    .build();

            BedrockAgentResponse agentResponse = bedrockAgentService.invokeAgent(agentRequest);
            response.put("bedrockAgentResponse", agentResponse);
            response.put("agentProcessingTime", agentResponse.getProcessingTimeMs() + "ms");

            if (agentResponse.isSuccess()) {
                response.put("agentStatus", "SUCCESS");
                log.info("Email successfully forwarded to Bedrock Agent with sessionId: {}", agentResponse.getSessionId());
            } else {
                response.put("agentStatus", "FAILED");
                log.warn("Bedrock Agent processing failed: {}", agentResponse.getMessage());
            }
            response.put("sessionId", agentResponse.getSessionId());
        } catch (Exception e) {
            log.error("Error forwarding email to Bedrock Agent", e);
            response.put("agentStatus", "ERROR");
            response.put("agentError", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
	

}
