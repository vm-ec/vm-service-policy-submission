package com.vm.service.policysubmission.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/policysubmission")
public class HealthController {
	
	@GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {

        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");

        return ResponseEntity.ok(response);
    }
	

}
