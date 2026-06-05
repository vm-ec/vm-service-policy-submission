# Example FNOL Email Request

```json
{
  "subject": "New Policy Claim - Vehicle Accident",
  "body": "I need to file a claim for policy POL-12345 due to a recent vehicle accident on Highway 101.",
  "sender": "john.doe@gmail.com",
  "recipients": ["claims@insurancecompany.com"],
  "queueId": "email-20260605-001",
  "timestamp": "2026-06-05T10:30:00Z",
  "attachmentPaths": ["/documents/accident_report.pdf", "/documents/photos_scene.jpg"]
}
```

# Example Response

```json
{
  "status": "RECEIVED",
  "bedrockAgentResponse": {
    "success": true,
    "message": "Email data successfully sent to Bedrock Agent for processing",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "agentResponse": {
      "claimStatus": "RECEIVED",
      "policyNumber": "POL-12345",
      "claimType": "FNOL"
    },
    "statusCode": 200
  },
  "agentStatus": "SUCCESS",
  "agentProcessingTime": "234ms"
}
```

# Test Commands

## FNOL Email
```bash
curl -X POST http://localhost:8080/fnol/email \
  -H "Content-Type: application/json" \
  -d @request.json
```

## Agent Details
```bash
curl http://localhost:8080/bedrock-agent/details
```

## Health Check
```bash
curl http://localhost:8080/bedrock-agent/health
```

