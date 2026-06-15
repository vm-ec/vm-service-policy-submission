package com.vm.service.policysubmission.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vm.service.policysubmission.dto.DynamoAgentRequest;
import com.vm.service.policysubmission.dto.DynamoAgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DynamoAgentService {

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
     * Invoke the Dynamo scoring pipeline (mock by default).
     */
    public DynamoAgentResponse invokeAgent(DynamoAgentRequest request) {
        try {
            log.info("Invoking Dynamo agent flow with agentId: {}, agentAliasId: {}", agentId, agentAliasId);

            String inputText = prepareInputText(request);
            log.debug("Prepared Dynamo request payload: {}", inputText);

            if (mockMode) {
                return simulateAgentProcessing(request);
            }

            // Live invocation hook for future Bedrock Agent Runtime integration.
            throw new UnsupportedOperationException(
                    "Live Dynamo Agent invocation is disabled. Set aws.bedrock.agent.mock-mode=true or add runtime wiring.");
        } catch (Exception e) {
            log.error("Error invoking Dynamo agent flow", e);
            return errorResponse(request, e);
        }
    }

    private String prepareInputText(DynamoAgentRequest request) {
        try {
            Map<String, Object> payload = Map.of(
                    "claim_reference", request != null ? request.getClaimReference() : null,
                    "structured_fields", request != null ? request.getStructuredFields() : null,
                    "claim_text", request != null ? request.getClaimText() : null,
                    "confidence_threshold", request != null ? request.getConfidenceThreshold() : null,
                    "agent_id", agentId,
                    "agent_alias_id", agentAliasId,
                    "runtime_client_configured", bedrockRuntimeClient != null
            );
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Dynamo request", e);
        }
    }

    /**
     * Simulate scoring response in the target Dynamo response schema.
     */
    private DynamoAgentResponse simulateAgentProcessing(DynamoAgentRequest request) {
        log.info("Processing Dynamo request in MOCK mode");

        List<DynamoAgentResponse.DetectedSignal> signals = new ArrayList<>();

        signals.add(DynamoAgentResponse.DetectedSignal.builder()
                .signalName("Claimant solicitor")
                .category("Litigation")
                .dimension("severity")
                .band("high")
                .bandDescription("Letter of Claim / Part 7 proceedings")
                .confidence(95)
                .evidenceSnippet("Claimant solicitor Smith & Co has sent a Letter of Claim")
                .applicableLift(3.6)
                .build());

        signals.add(DynamoAgentResponse.DetectedSignal.builder()
                .signalName("Personal injury escalation")
                .category("Injury")
                .dimension("severity")
                .band("high")
                .bandDescription("Serious injury, surgery")
                .confidence(90)
                .evidenceSnippet("Cyclist sustained serious leg injuries requiring surgery")
                .applicableLift(3.0)
                .build());

        signals.add(DynamoAgentResponse.DetectedSignal.builder()
                .signalName("Credit hire & associated charges")
                .category("Credit hire")
                .dimension("severity")
                .band("mid")
                .bandDescription("Prolonged hire, CHO involved")
                .confidence(80)
                .evidenceSnippet("Credit hire vehicle on prolonged hire via CHO")
                .applicableLift(2.2)
                .build());

        signals.add(DynamoAgentResponse.DetectedSignal.builder()
                .signalName("Liability against insured")
                .category("Liability")
                .dimension("severity")
                .band("mid")
                .bandDescription("Clear fault")
                .confidence(70)
                .evidenceSnippet("Liability disputed - conflicting accounts")
                .applicableLift(1.8)
                .build());

        return DynamoAgentResponse.builder()
                .claimReference(request != null ? request.getClaimReference() : null)
                .detectedSignals(signals)
                .severityIndex(78)
                .severityBand("High")
                .complexityBand("SIMPLE")
                .routingDecision("Senior handler")
                .rationale("Claimant solicitor (high): \"Claimant solicitor Smith & Co has sent a Letter of Claim\"; "
                        + "Personal injury escalation (high): \"Cyclist sustained serious leg injuries requiring surgery\"; "
                        + "Credit hire & associated charges (mid): \"Credit hire vehicle on prolonged hire via CHO\"; "
                        + "Liability against insured (mid): \"Liability disputed - conflicting accounts\"")
                .disclaimer("Illustrative rule-based index. No monetary projection produced in MVP1.")
                .build();
    }

    private DynamoAgentResponse errorResponse(DynamoAgentRequest request, Exception e) {
        return DynamoAgentResponse.builder()
                .claimReference(request != null ? request.getClaimReference() : null)
                .detectedSignals(List.of())
                .severityIndex(0)
                .severityBand("Unknown")
                .complexityBand("SIMPLE")
                .routingDecision("Manual review")
                .rationale("Agent processing failed: " + e.getMessage())
                .disclaimer("Fallback response generated due to processing error.")
                .build();
    }
}
