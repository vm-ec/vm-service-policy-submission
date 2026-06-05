package com.vm.service.policysubmission.service;

import com.vm.service.policysubmission.dto.BedrockAgentRequest;
import com.vm.service.policysubmission.dto.BedrockAgentResponse;
import com.vm.service.policysubmission.dto.EmailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailReceiveService {

    @Autowired
    private BedrockAgentService bedrockAgentService;

    /**
     * Push email data to Bedrock Agent for processing
     */
    public BedrockAgentResponse pushToBedrock(EmailRequest email) {
        log.info("Pushing email to Bedrock Agent for processing. Sender: {}, Subject: {}",
            email.getSender(), email.getSubject());

        try {
            BedrockAgentRequest agentRequest = BedrockAgentRequest.builder()
                    .subject(email.getSubject())
                    .body(email.getBody())
                    .sender(email.getSender())
                    .timestamp(email.getTimestamp())
                    .queueId(email.getQueueId())
                    .recipients(email.getRecipients())
                    .attachmentPaths(email.getAttachmentPaths())
                    .inputText(String.format("Process FNOL email from %s with subject: %s",
                        email.getSender(), email.getSubject()))
                    .build();

            BedrockAgentResponse response = bedrockAgentService.invokeAgent(agentRequest);
            log.info("Email pushed to Bedrock Agent successfully. Session ID: {}", response.getSessionId());
            return response;
        } catch (Exception e) {
            log.error("Error pushing email to Bedrock Agent", e);
            return createErrorResponse(e);
        }
    }

    /**
     * Create an error response for failed Bedrock Agent invocations
     */
    private BedrockAgentResponse createErrorResponse(Exception e) {
        return BedrockAgentResponse.builder()
                .success(false)
                .statusCode(500)
                .message("Error pushing email to Bedrock Agent: " + e.getMessage())
                .metadata(new HashMap<String, Object>() {{
                    put("errorDetails", e.getClass().getSimpleName() + " - " + e.getMessage());
                }})
                .build();
    }
}
