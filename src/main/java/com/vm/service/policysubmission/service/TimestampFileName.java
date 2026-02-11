package com.vm.service.policysubmission.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimestampFileName {

    public String addTimestamp(String originalFileName) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

        String timestamp = LocalDateTime.now().format(formatter);

        int dotIndex = originalFileName.lastIndexOf(".");

        if (dotIndex == -1) {
            // No extension
            return originalFileName + "_" + timestamp;
        }

        String name = originalFileName.substring(0, dotIndex);
        String extension = originalFileName.substring(dotIndex);

        return name + "_" + timestamp + extension;
    }
}


