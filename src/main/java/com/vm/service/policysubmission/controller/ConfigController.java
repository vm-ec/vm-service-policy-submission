package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/claims-check")
public class ConfigController {

    @Autowired
    ConfigService configService;

    @PostMapping
    public String save() {

        configService.save(
                "APP001",
                "PaymentService");

        return "Success";
    }

    @GetMapping("/claims/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String id) {
        return ResponseEntity.ok(Map.copyOf(configService.getItem(id)));
    }

    @GetMapping("/claims")
    public ResponseEntity<List<Map<String, Object>>> getAllClaims() {
        return ResponseEntity.ok(configService.scanAllItems());
    }
}
