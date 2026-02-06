package com.vm.service.policysubmission.dto;
// File: 'src/main/java/com/vm/service/policysubmission/dto/EmailRequest.java'
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class EmailRequest {
    private String subject;
    private String body;
    private String sender;
    private Instant timestamp;
    private String queueId;
    private List<String> recipients;
    private List<String> attachmentPaths;

}

