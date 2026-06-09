package com.vm.service.policysubmission.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {

    private static final String AWS_ACCESS_KEY =
            System.getenv("DYNAMO_AWS_ACCESS_KEY_ID");

    private static final String AWS_SECRET_KEY =
            System.getenv("DYNAMO_AWS_SECRET_ACCESS_KEY");

    private static final String AWS_REGION =
            System.getenv("AWS_REGION");

    @Bean
    public DynamoDbClient dynamoDbClient() {

        return DynamoDbClient.builder()
                .region(Region.of(AWS_REGION))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        AWS_ACCESS_KEY,
                                        AWS_SECRET_KEY)
                        )
                )
                .build();
    }
}