package com.vm.service.policysubmission.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    // Optional override; otherwise region is resolved from env/profile/IMDS
    @Value("${aws.bedrock.region:us-east-1}")
    private String regionProperty;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        Region resolvedRegion = resolveRegion();
        return BedrockRuntimeClient.builder()
                .region(resolvedRegion)
                // Default credentials chain (env, system props, web identity, shared config incl. SSO, ECS/EC2)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private Region resolveRegion() {
        // Use property if provided
        if (regionProperty != null && !regionProperty.isBlank()) {
            return Region.of(regionProperty);
        }
        // Else try default chain
        try {
            AwsRegionProvider chain = DefaultAwsRegionProviderChain.builder().build();
            Region fromChain = chain.getRegion();
            if (fromChain != null) {
                return fromChain;
            }
        } catch (Exception ignored) {
            // fall through
        }
        // Fallback
        return Region.US_EAST_1;
    }
}

