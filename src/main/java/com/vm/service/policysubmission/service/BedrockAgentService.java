package com.vm.service.policysubmission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vm.service.policysubmission.dto.BedrockAgentRequest;
import com.vm.service.policysubmission.dto.BedrockAgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class BedrockAgentService {

    @Autowired
    private BedrockRuntimeClient bedrockRuntimeClient;

    @Value("${aws.bedrock.agent.id:MOCK-AGENT-ID}")
    private String agentId;

    @Value("${aws.bedrock.agent.alias.id:LFZQYFUWQZ}")
    private String agentAliasId;

    @Value("${aws.bedrock.agent.mock-mode:true}")
    private boolean mockMode;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Invoke the Bedrock Agent with email data
     */
    public BedrockAgentResponse invokeAgent(BedrockAgentRequest request) {
        long startTime = System.currentTimeMillis();
        BedrockAgentResponse response = BedrockAgentResponse.builder()
                .processingTimeMs(0)
                .traceEvents(new ArrayList<>())
                .metadata(new HashMap<>())
                .build();

        try {
            log.info("Invoking Bedrock Agent with agentId: {}, agentAliasId: {}", agentId, agentAliasId);

            // Generate session ID if not provided
            String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : UUID.randomUUID().toString();
            response.setSessionId(sessionId);

            // Prepare the input text with email details
            String inputText = prepareInputText(request);
            log.debug("Input text prepared for agent invocation");

            String agentResponse;
            if (mockMode) {
                agentResponse = simulateAgentProcessing(inputText, request);
            } else {
                // Live invocation hook: keep explicit until real AWS Agent Runtime wiring is enabled.
                throw new UnsupportedOperationException(
                        "Live Bedrock Agent invocation is disabled in this build. Set aws.bedrock.agent.mock-mode=true or add runtime wiring.");
            }

            response.setSuccess(true);
            response.setStatusCode(200);
            response.setAgentResponse(agentResponse);
            response.setMessage("Email data processed by Bedrock Agent pipeline");

            // Extract metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("agentId", agentId);
            metadata.put("agentAliasId", agentAliasId);
            metadata.put("sessionId", sessionId);
            metadata.put("email_subject", request.getSubject());
            metadata.put("email_sender", request.getSender());
            metadata.put("executionMode", mockMode ? "MOCK" : "LIVE");
            metadata.put("runtimeClientConfigured", bedrockRuntimeClient != null);
            response.setMetadata(metadata);

            log.info("Bedrock Agent invocation successful");

        } catch (Exception e) {
            log.error("Error invoking Bedrock Agent", e);
            response.setSuccess(false);
            response.setStatusCode(500);
            response.setMessage("Error invoking Bedrock Agent: " + e.getMessage());
            response.getMetadata().put("errorDetails", e.getClass().getSimpleName() + " - " + e.getMessage());
        } finally {
            response.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        }

        return response;
    }

    /**
     * Prepare the input text to send to the Bedrock Agent
     */
    private String prepareInputText(BedrockAgentRequest request) {
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("subject", request.getSubject());
            emailData.put("body", request.getBody());
            emailData.put("sender", request.getSender());
            emailData.put("timestamp", request.getTimestamp() != null ? request.getTimestamp().toString() : Instant.now().toString());
            emailData.put("queueId", request.getQueueId());
            emailData.put("recipients", request.getRecipients());
            emailData.put("attachmentPaths", request.getAttachmentPaths());

            String jsonPayload = objectMapper.writeValueAsString(emailData);
            
            // Create a prompt for the agent
            String prompt = String.format(
                "Process the following FNOL (First Notice of Loss) email and extract key information:\n\n%s\n\n" +
                "Please analyze the email content, extract policy information, claim details, and any attachments mentioned. " +
                "Provide a structured summary of the claim information.",
                jsonPayload
            );

            return prompt;
        } catch (Exception e) {
            log.error("Error preparing input text", e);
            throw new RuntimeException("Failed to prepare input text for Bedrock Agent", e);
        }
    }

    /**
     * Simulate Bedrock Agent processing (for development/testing)
     * In production, this would call actual Bedrock Agent API
     */
    private String simulateAgentProcessing(String inputText, BedrockAgentRequest request) {
        log.info("Processing email through simulated Bedrock Agent");
        
        Map<String, Object> processedClaim = new HashMap<>();
        processedClaim.put("claimStatus", "RECEIVED");
        processedClaim.put("policyNumber", extractPolicyNumber(request.getBody()));
        processedClaim.put("claimType", "FNOL");
        processedClaim.put("claimDescription", request.getSubject());
        processedClaim.put("policyholder", request.getSender());
        processedClaim.put("attachmentCount", request.getAttachmentPaths() != null ? request.getAttachmentPaths().size() : 0);
        processedClaim.put("extractionStatus", "SUCCESS");
        processedClaim.put("timestamp", Instant.now().toString());
        processedClaim.put("agentSummary", "FNOL email successfully processed. Claim information extracted and ready for review.");

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(processedClaim);
        } catch (Exception e) {
            log.error("Error formatting agent response", e);
            return "Error processing agent response";
        }
    }

    /**
     * Extract policy number from email body (simple regex pattern)
     */
    private String extractPolicyNumber(String emailBody) {
        if (emailBody == null || emailBody.isEmpty()) {
            return "NOT_FOUND";
        }
        
        // Simple regex pattern for policy numbers (format: POL-XXXXX or similar)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("POL-\\d{5}|Policy[\\s#]*(\\d{8,})");
        java.util.regex.Matcher matcher = pattern.matcher(emailBody);
        
        if (matcher.find()) {
            return matcher.group();
        }
        return "NOT_FOUND";
    }

    /**
     * Get mock agent details (for testing without actual Bedrock setup)
     */
    public Map<String, Object> getMockAgentDetails() {
        Map<String, Object> agentDetails = new HashMap<>();
        agentDetails.put("agentId", agentId);
        agentDetails.put("agentAliasId", agentAliasId);
        agentDetails.put("agentName", "Policy Submission FNOL Agent");
        agentDetails.put("agentDescription", "Agent for processing First Notice of Loss (FNOL) emails from insurance claims");
        agentDetails.put("knowledgeBaseId", "MOCK-KB-ID-12345");
        agentDetails.put("knowledgeBaseName", "Insurance Policy Knowledge Base");
        agentDetails.put("capabilities", new String[]{
            "Extract policyholder information",
            "Identify claim type and loss description",
            "Extract policy number and dates",
            "Process attachments and documents",
            "Generate claim summary"
        });
        agentDetails.put("status", "ACTIVE");
        agentDetails.put("createdAt", Instant.now().toString());
        
        return agentDetails;
    }
}
