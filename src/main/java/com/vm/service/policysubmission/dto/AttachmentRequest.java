package com.vm.service.policysubmission.dto;

import lombok.Data;

@Data
public class AttachmentRequest {
    private String fileName;
    private String contentType;
    private String contentBytes;
}


