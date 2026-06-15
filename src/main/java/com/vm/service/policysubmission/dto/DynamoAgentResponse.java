package com.vm.service.policysubmission.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DynamoAgentResponse {

    private String claimReference;
    private List<DetectedSignal> detectedSignals;
    private Integer severityIndex;
    private String severityBand;
    private String complexityBand;
    private String routingDecision;
    private String rationale;
    private String disclaimer;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DetectedSignal {
        private String signalName;
        private String category;
        private String dimension;
        private String band;
        private String bandDescription;
        private Integer confidence;
        private String evidenceSnippet;
        private Double applicableLift;
    }
}
