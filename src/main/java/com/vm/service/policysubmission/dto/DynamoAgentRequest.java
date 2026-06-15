package com.vm.service.policysubmission.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DynamoAgentRequest {

    private String claimReference;
    private StructuredFields structuredFields;
    private String claimText;
    private Integer confidenceThreshold;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class StructuredFields {
        private String cover;
        private String incidentType;
        private Boolean injuryIndicator;
    }
}
