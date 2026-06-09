package com.vm.service.policysubmission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final DynamoDbClient dynamoDbClient;

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
                        .tableName("CLAIMS_SEVERITY")
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
    public Map<String, Object> getItem(String id) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName("CLAIMS_SEVERITY")
                // use the table's partition key name (claim_reference) rather than "id"
                .key(Map.of("claim_reference", AttributeValue.builder().s(id).build()))
                .build();

        Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();
        if (item == null || item.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, AttributeValue> e : item.entrySet()) {
            result.put(e.getKey(), attributeValueToObject(e.getValue()));
        }

        return result;
    }

    public List<Map<String, Object>> scanAllItems() {
        ScanRequest request = ScanRequest.builder()
                .tableName("CLAIMS_SEVERITY")
                .build();

        ScanResponse response = dynamoDbClient.scan(request);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, AttributeValue> item : response.items()) {
            Map<String, Object> convertedItem = new HashMap<>();
            for (Map.Entry<String, AttributeValue> e : item.entrySet()) {
                convertedItem.put(e.getKey(), attributeValueToObject(e.getValue()));
            }
            result.add(convertedItem);
        }

        return result;
    }
    private Object attributeValueToObject(AttributeValue av) {
        if (av == null) return null;
        if (av.s() != null) return av.s();
        if (av.n() != null) {
            // keep numbers as string to avoid precision/BigDecimal surprises in this small demo
            return av.n();
        }
        if (av.bool() != null) return av.bool();
        //if (av.nullValue() != null && av.nullValue()) return null;
        if (av.hasM() && av.m() != null && !av.m().isEmpty()) {
            Map<String, Object> nested = new HashMap<>();
            for (Map.Entry<String, AttributeValue> me : av.m().entrySet()) {
                nested.put(me.getKey(), attributeValueToObject(me.getValue()));
            }
            return nested;
        }
        if (av.hasL() && av.l() != null && !av.l().isEmpty()) {
            List<Object> list = new ArrayList<>();
            for (AttributeValue a : av.l()) {
                list.add(attributeValueToObject(a));
            }
            return list;
        }
        if (av.ss() != null && !av.ss().isEmpty()) return new ArrayList<>(av.ss());
        if (av.ns() != null && !av.ns().isEmpty()) return new ArrayList<>(av.ns());

        // fallback to the raw AttributeValue as a toString if nothing matched
        return av.toString();
    }
}