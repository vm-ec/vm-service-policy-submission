package com.vm.service.policysubmission.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConfigService {

    private final DynamoDbClient dynamoDbClient;

    public ConfigService(
            DynamoDbClient dynamoDbClient) {

        this.dynamoDbClient = dynamoDbClient;
    }

    public void save(
            String configId,
            String serviceName) {

        Map<String, AttributeValue> item =
                new HashMap<>();

        item.put(
                "configId",
                AttributeValue.builder()
                        .s(configId)
                        .build());

        item.put(
                "serviceName",
                AttributeValue.builder()
                        .s(serviceName)
                        .build());

        dynamoDbClient.putItem(
                PutItemRequest.builder()
                        .tableName("CustomerConfig")
                        .item(item)
                        .build());
    }
    public String get() {
        Map<String, AttributeValue> key = new HashMap<>();

        key.put("configId",
                AttributeValue.builder()
                        .s("APP001")
                        .build());

        GetItemRequest request =
                GetItemRequest.builder()
                        .tableName("CustomerConfig")
                        .key(key)
                        .build();

        GetItemResponse response =
                dynamoDbClient.getItem(request);

        String serviceName =
                response.item()
                        .get("serviceName")
                        .s();

        return serviceName;
    }
}