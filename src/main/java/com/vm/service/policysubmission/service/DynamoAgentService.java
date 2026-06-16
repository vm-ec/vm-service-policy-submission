package com.vm.service.policysubmission.service;

import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vm.service.policysubmission.dto.DynamoAgentRequest;
import com.vm.service.policysubmission.dto.DynamoAgentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentResponse;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class DynamoAgentService {

    @Autowired
    private BedrockAgentRuntimeClient bedrockAgentRuntimeClient;

    @Value("${aws.bedrock.agent.id:claim_severity_mvp}")
    private String agentId;

    @Value("${aws.bedrock.agent.alias.id:claim_severity_mvp-MvaaGWBA7C}")
    private String agentAliasId;

    @Value("${aws.bedrock.agent.runtime.arn:arn:aws:bedrock-agentcore:us-east-1:171161696846:runtime/claim_severity_mvp-MvaaGWBA7C}")
    private String agentRuntimeArn;

    @Value("${aws.bedrock.agent.runtime.arn:arn:us-east-1}")
    private String region;

    @Value("${aws.bedrock.agent.mock-mode:false}")
    private boolean mockMode;
    
    private static final String AWS_ACCESS_KEY = System.getenv("DYNAMO_AWS_ACCESS_KEY_ID");

    private static final String AWS_SECRET_KEY = System.getenv("DYNAMO_AWS_SECRET_ACCESS_KEY");
 

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Invoke the Dynamo scoring pipeline (mock by default).
     */
    /*public DynamoAgentResponse invokeAgent(DynamoAgentRequest request) {
        try {
            log.info("Invoking Dynamo agent flow with agentId: {}, agentAliasId: {}", agentId, agentAliasId);

            String inputText = prepareInputText(request);
            log.debug("Prepared Dynamo request payload: {}", inputText);

            *//*if (mockMode) {
                return simulateAgentProcessing(request);
            }*//*

            // Live invocation hook for future Bedrock Agent Runtime integration.
            throw new UnsupportedOperationException(
                    "Live Dynamo Agent invocation is disabled. Set aws.bedrock.agent.mock-mode=true or add runtime wiring.");
        } catch (Exception e) {
            log.error("Error invoking Dynamo agent flow", e);
            return errorResponse(request, e);
        }
    }*/

    public DynamoAgentResponse invokeAgent(DynamoAgentRequest request) {
        try {
            log.info("Invoking Dynamo agent flow with agentId: {}, agentAliasId: {}", agentId, agentAliasId);

            String inputText = prepareInputText(request);
            log.debug("Prepared Dynamo request payload: {}", inputText);

            //String agentRuntimeArn = agentRuntimeArn;
            Region region = Region.US_EAST_1;

            if (agentRuntimeArn == null || agentRuntimeArn.isEmpty()) {
                throw new IllegalArgumentException("agentRuntimeArn is required for live Bedrock Agent Core invocation");
            }

            // Send raw text as payload (most agents accept this format)
            String payload = prepareInputText(request);

            InvokeAgentRuntimeRequest invokeRequest = InvokeAgentRuntimeRequest.builder()
                    .agentRuntimeArn(agentRuntimeArn)
                    .qualifier("DEFAULT")
                    .contentType("text/plain")
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build();

            log.info("Invoking Bedrock Agent Core with ARN: {}", agentRuntimeArn);

           BedrockAgentCoreClient bedrockClient = BedrockAgentCoreClient.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY)
        ))
        .build();

            String agentOutput;
            try (var response = bedrockClient.invokeAgentRuntime(invokeRequest)) {
                agentOutput = new String(response.readAllBytes(), StandardCharsets.UTF_8);
                log.info("Bedrock Agent Core response received: {}", agentOutput);
            }

            // Parse and convert agent response to DynamoAgentResponse
            return parseAgentResponse(agentOutput, request);

        } catch (Exception e) {
            log.error("Error invoking Dynamo agent flow", e);
            return errorResponse(request, e);
        }
    }

    /**
     * Parses the agent's JSON response and converts it to DynamoAgentResponse
     */
    private DynamoAgentResponse parseAgentResponse(String agentOutput, DynamoAgentRequest request) {
        try {
            // The agent should return JSON with snake_case fields matching DynamoAgentResponse
            DynamoAgentResponse response = objectMapper.readValue(
                    agentOutput,
                    DynamoAgentResponse.class
            );

            // Set claim reference from request if not in response
            if (response.getClaimReference() == null) {
                response.setClaimReference(request.getClaimReference());
            }

            // Set request ID and timestamp
            if (response.getClaimReference() != null) {
                log.info("Successfully parsed agent response for claim: {}", response.getClaimReference());
            }

            return response;

        } catch (Exception e) {
            log.error("Failed to parse agent response JSON: {}", agentOutput, e);

            // Try to extract JSON if agent output has extra text
            try {
                String jsonStr = extractJsonFromOutput(agentOutput);
                DynamoAgentResponse response = objectMapper.readValue(jsonStr, DynamoAgentResponse.class);

                if (response.getClaimReference() == null) {
                    response.setClaimReference(request.getClaimReference());
                }

                return response;
            } catch (Exception parseEx) {
                log.error("Failed to extract and parse JSON from agent output", parseEx);
                return errorResponse(request, e);
            }
        }
    }

    /**
     * Extracts JSON from agent output that might have surrounding text
     */
    private String extractJsonFromOutput(String output) {
        // Find the first '{' and last '}'
        int start = output.indexOf('{');
        int end = output.lastIndexOf('}');

        if (start >= 0 && end > start) {
            return output.substring(start, end + 1);
        }
        return output;
    }

    // Helper method to convert agent output to DynamoAgentResponse
    /*private DynamoAgentResponse buildDynamoAgentResponse(DynamoAgentRequest request, String agentOutput) {
        return DynamoAgentResponse.builder()
                .requestId(request.getRequestId())
                .status("SUCCESS")
                .output(agentOutput)
                .timestamp(System.currentTimeMillis())
                .build();
    }*/

    /**
     * Map the agent's JSON response to DynamoAgentResponse.
     * First attempts strict mapping, then falls back to flexible mapping.
     */
    private DynamoAgentResponse mapResponseToDynamo(String agentResponse, DynamoAgentRequest request) throws Exception {
        // Try strict mapping first
        try {
            DynamoAgentResponse mapped = objectMapper.readValue(agentResponse, DynamoAgentResponse.class);
            if (mapped.getClaimReference() == null && request != null) {
                mapped.setClaimReference(request.getClaimReference());
            }
            log.info("Mapped response using strict mapping");
            return mapped;
        } catch (Exception strictEx) {
            log.debug("Strict mapping failed, trying flexible mapping", strictEx);
        }

        // Flexible mapping as fallback
        try {
            Map<String, Object> parsed = objectMapper.readValue(agentResponse, Map.class);
            DynamoAgentResponse response = new DynamoAgentResponse();

            // Map claim_reference
            Object cr = parsed.getOrDefault("claim_reference", parsed.get("claimReference"));
            response.setClaimReference(cr != null ? cr.toString() : (request != null ? request.getClaimReference() : null));

            // Map detected_signals
            Object signalsObj = parsed.getOrDefault("detected_signals", parsed.get("detectedSignals"));
            if (signalsObj instanceof List) {
                List<?> rawList = (List<?>) signalsObj;
                List<DynamoAgentResponse.DetectedSignal> signals = new ArrayList<>();
                for (Object o : rawList) {
                    try {
                        DynamoAgentResponse.DetectedSignal s = objectMapper.convertValue(o, DynamoAgentResponse.DetectedSignal.class);
                        signals.add(s);
                    } catch (Exception e) {
                        log.debug("Could not convert signal element", e);
                    }
                }
                response.setDetectedSignals(signals);
            } else {
                response.setDetectedSignals(List.of());
            }

            // Map severity_index
            Object si = parsed.getOrDefault("severity_index", parsed.get("severityIndex"));
            if (si != null) {
                try {
                    response.setSeverityIndex(Integer.parseInt(si.toString()));
                } catch (Exception ignore) {
                    response.setSeverityIndex(null);
                }
            }

            // Map other fields
            Object sb = parsed.getOrDefault("severity_band", parsed.get("severityBand"));
            response.setSeverityBand(sb != null ? sb.toString() : "Unknown");

            Object cb = parsed.getOrDefault("complexity_band", parsed.get("complexityBand"));
            response.setComplexityBand(cb != null ? cb.toString() : "SIMPLE");

            Object rd = parsed.getOrDefault("routing_decision", parsed.get("routingDecision"));
            response.setRoutingDecision(rd != null ? rd.toString() : "Manual review");

            Object rat = parsed.getOrDefault("rationale", parsed.get("explanation"));
            response.setRationale(rat != null ? rat.toString() : agentResponse);

            Object disc = parsed.getOrDefault("disclaimer", parsed.get("note"));
            response.setDisclaimer(disc != null ? disc.toString() : "Generated by agent");

            log.info("Mapped response using flexible mapping");
            return response;
        } catch (Exception flexibleEx) {
            log.error("Both mapping attempts failed", flexibleEx);
            throw flexibleEx;
        }
    }


    private String prepareInputText(DynamoAgentRequest request) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("claim_reference", request != null ? request.getClaimReference() : null);
            payload.put("structured_fields", request != null ? request.getStructuredFields() : null);
            payload.put("claim_text", request != null ? request.getClaimText() : null);
            payload.put("confidence_threshold", request != null ? request.getConfidenceThreshold() : null);
            payload.put("agent_id", agentId);
            payload.put("agent_alias_id", agentAliasId);
            payload.put("runtime_client_configured", bedrockAgentRuntimeClient != null);
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

    /**
     * Generate a session ID (must be 33+ characters for Agent Runtime).
     * Creates a UUID-based ID with prefix to ensure minimum length.
     */
    private String generateSessionId() {
        // UUID is 36 characters (including hyphens), so this will always be > 33 chars
        String sessionId = "session-" + UUID.randomUUID();
        log.debug("Generated sessionId: {} (length: {})", sessionId, sessionId.length());
        return sessionId;
    }

    /**
     * Read the streaming response from InvokeAgentResponse.
     * The Agent Runtime API returns a streaming body that must be fully consumed.
     */
    /*private String readResponseStream(InvokeAgentResponse response) {
        try {
            if (response == null) {
                return null;
            }
            // InvokeAgentResponse.output() returns an event stream
            // We need to collect all events and extract the final response text
            StringBuilder fullResponse = new StringBuilder();

            // Iterate through response events
            if (response.output() != null) {
                response.output().forEach(event -> {
                    try {
                        // Each event may contain trace or final response
                        if (event.text() != null) {
                            fullResponse.append(event.text());
                        }
                    } catch (Exception e) {
                        log.debug("Error processing response event", e);
                    }
                });
            }

            return fullResponse.toString();
        } catch (Exception e) {
            log.error("Error reading response stream", e);
            throw new RuntimeException("Failed to read Bedrock Agent Runtime response", e);
        }
    }*/
}
