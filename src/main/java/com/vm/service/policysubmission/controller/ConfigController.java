package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
