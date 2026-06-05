package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.dto.BedrockAgentResponse;
import com.vm.service.policysubmission.dto.ContextEmailRequest;
import com.vm.service.policysubmission.dto.EmailRequest;
import com.vm.service.policysubmission.service.EmailReceiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/claims-fnol")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Claims FNOL", description = "APIs for processing claims FNOL emails through Bedrock Agent")
public class EmailReceiveController {
    private final EmailReceiveService emailReceiveService;

    @PostMapping(path = "/email", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Process Claims FNOL Email", description = "Receive claims FNOL email and forward to Bedrock Agent for processing")
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

        try {
            BedrockAgentResponse agentResponse = emailReceiveService.pushToBedrock(request);
            response.put("bedrockResponse", agentResponse);
            response.put("agentStatus", agentResponse.isSuccess() ? "SUCCESS" : "FAILED");
            response.put("sessionId", agentResponse.getSessionId());
            log.info("Email successfully forwarded to Bedrock Agent");
        } catch (Exception e) {
            log.error("Error processing claims FNOL email", e);
            response.put("agentStatus", "ERROR");
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
