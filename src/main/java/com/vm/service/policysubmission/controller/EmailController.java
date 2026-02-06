package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.dto.EmailRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fnol")
public class EmailController {

    @PostMapping(path = "/email", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> process(@RequestBody EmailRequest request) {
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

        return ResponseEntity.ok(response);
    }
	

}
