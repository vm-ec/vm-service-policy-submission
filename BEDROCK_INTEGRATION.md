# Amazon Bedrock Agent Integration Guide

## Overview
This application has been enhanced with Amazon Bedrock Agent integration for processing First Notice of Loss (FNOL) emails. The integration allows automatic processing and analysis of insurance claims through AWS Bedrock Agents.

## New Components Added

### 1. Configuration Classes
- **BedrockConfig.java** - Spring configuration for AWS Bedrock Runtime client
  - Automatically uses DefaultCredentialsProvider for authentication
  - Supports environment-based region configuration
  - Falls back to Region.US_EAST_1 if no region is configured

### 2. Services
- **BedrockAgentService.java** - Core service for interacting with Bedrock
  - `invokeAgent(BedrockAgentRequest)` - Invokes the agent with email data
  - `getMockAgentDetails()` - Returns mock agent configuration for testing
  - Includes policy number extraction from email body
  - Simulates agent processing for development/testing

- **EmailReceiveService.java** - Enhanced with Bedrock integration
  - `pushToBedrock(EmailRequest)` - Forwards emails to Bedrock Agent
  - Error handling and response mapping

### 3. Controllers
- **BedrockAgentController.java** - Dedicated agent management endpoints
  - `POST /bedrock-agent/invoke` - Invoke agent with BedrockAgentRequest
  - `GET /bedrock-agent/details` - Get agent configuration details
  - `GET /bedrock-agent/health` - Health check for agent service

- **EmailController.java** - Enhanced FNOL email processing
  - `POST /fnol/email` - Receive FNOL email and forward to Bedrock Agent
  - Now integrates with BedrockAgentService automatically

- **EmailReceiveController.java** - Enhanced claims FNOL processing
  - `POST /claims-fnol/email` - Process claims FNOL email through Bedrock
  - Forwards to Bedrock Agent for analysis

### 4. Data Transfer Objects (DTOs)
- **BedrockAgentRequest.java** - Request payload for Bedrock Agent
  - Contains email details (subject, body, sender, recipients, attachments)
  - Session management fields
  - Additional context variables

- **BedrockAgentResponse.java** - Response from Bedrock Agent
  - Success status and status code
  - Agent response content
  - Processing metadata and timing
  - Trace events for debugging

## Configuration

### Application Properties
Add to `application.properties`:

```properties
# AWS Bedrock Configuration
aws.bedrock.region=us-east-1
aws.bedrock.agent.id=YOUR-AGENT-ID
aws.bedrock.agent.alias.id=YOUR-AGENT-ALIAS-ID
```

### Environment Variables (for Render deployment)
Set these environment variables in Render:

```bash
# AWS Credentials (preferably use IAM role instead)
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# AWS Region
AWS_REGION=us-east-1

# Bedrock Agent Configuration
AWS_BEDROCK_AGENT_ID=your-agent-id
AWS_BEDROCK_AGENT_ALIAS_ID=your-agent-alias-id
```

## API Endpoints

### 1. FNOL Email Processing
**POST** `/fnol/email`

Request:
```json
{
  "subject": "Policy Claim - Car Accident",
  "body": "I have a claim for policy POL-12345 due to a car accident",
  "sender": "customer@example.com",
  "recipients": ["claims@insurance.com"],
  "queueId": "email-queue-123",
  "timestamp": "2026-06-05T12:00:00Z",
  "attachmentPaths": ["/attachments/document1.pdf"]
}
```

Response:
```json
{
  "status": "RECEIVED",
  "subject": "Policy Claim - Car Accident",
  "sender": "customer@example.com",
  "recipients": ["claims@insurance.com"],
  "queueId": "email-queue-123",
  "timestamp": "2026-06-05T12:00:00Z",
  "bodyLength": 67,
  "attachmentPaths": ["/attachments/document1.pdf"],
  "message": "Email payload acknowledged",
  "bedrockAgentResponse": {
    "success": true,
    "message": "Email data successfully sent to Bedrock Agent for processing",
    "sessionId": "uuid-session-id",
    "agentResponse": "{...processed claim data...}",
    "statusCode": 200,
    "metadata": {...},
    "processingTimeMs": 234
  },
  "sessionId": "uuid-session-id",
  "agentStatus": "SUCCESS",
  "agentProcessingTime": "234ms"
}
```

### 2. Claims FNOL Email Processing
**POST** `/claims-fnol/email`

Same request/response format as `/fnol/email`

### 3. Bedrock Agent Invocation
**POST** `/bedrock-agent/invoke`

Request:
```json
{
  "subject": "Claim Submission",
  "body": "Policy POL-99999 claim details...",
  "sender": "user@example.com",
  "timestamp": "2026-06-05T12:00:00Z",
  "queueId": "queue-001",
  "recipients": ["support@company.com"],
  "attachmentPaths": []
}
```

### 4. Get Agent Details
**GET** `/bedrock-agent/details`

Response:
```json
{
  "agentId": "MOCK-AGENT-ID",
  "agentAliasId": "LFZQYFUWQZ",
  "agentName": "Policy Submission FNOL Agent",
  "agentDescription": "Agent for processing First Notice of Loss (FNOL) emails",
  "knowledgeBaseId": "MOCK-KB-ID-12345",
  "knowledgeBaseName": "Insurance Policy Knowledge Base",
  "capabilities": [
    "Extract policyholder information",
    "Identify claim type and loss description",
    "Extract policy number and dates",
    "Process attachments and documents",
    "Generate claim summary"
  ],
  "status": "ACTIVE",
  "createdAt": "2026-06-05T12:00:00Z"
}
```

### 5. Agent Health Check
**GET** `/bedrock-agent/health`

Response:
```json
{
  "status": "UP",
  "service": "Bedrock Agent Service",
  "timestamp": "2026-06-05T12:00:00Z"
}
```

## AWS Credentials Management

### Local Development
1. Install AWS CLI
2. Configure credentials: `aws configure`
3. Credentials will be automatically picked up by DefaultCredentialsProvider

### Production (Render/AWS)

#### Option 1: IAM Role (Recommended)
- Attach IAM role to EC2 instance or ECS task
- Service uses InstanceProfileCredentialsProvider automatically
- No credentials in environment variables needed

#### Option 2: Environment Variables
Set in Render environment:
```bash
AWS_ACCESS_KEY_ID=xxxx
AWS_SECRET_ACCESS_KEY=xxxx
AWS_REGION=us-east-1
```

#### Option 3: AWS Credentials File
Mount credentials file at `/home/ubuntu/.aws/credentials`

## Credential Provider Chain
The application uses AWS SDK's DefaultCredentialsProvider which attempts:
1. SystemPropertyCredentialsProvider (Java system properties)
2. EnvironmentVariableCredentialsProvider (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
3. WebIdentityTokenCredentialsProvider (for OIDC)
4. ProfileCredentialsProvider (shared credentials file)
5. ContainerCredentialsProvider (ECS/Fargate)
6. InstanceProfileCredentialsProvider (EC2 IAM role)

## Deployment to Render

### 1. Update Environment Variables
In Render dashboard:
- Set `AWS_REGION=us-east-1`
- Configure agent IDs through properties or environment:
  ```bash
  AWS_BEDROCK_AGENT_ID=your-agent-id
  AWS_BEDROCK_AGENT_ALIAS_ID=your-agent-alias-id
  ```

### 2. Update application.properties
```properties
aws.bedrock.region=${AWS_REGION:us-east-1}
aws.bedrock.agent.id=${AWS_BEDROCK_AGENT_ID:MOCK-AGENT-ID}
aws.bedrock.agent.alias.id=${AWS_BEDROCK_AGENT_ALIAS_ID:LFZQYFUWQZ}
```

### 3. Attach IAM Role (Recommended)
Instead of using access keys:
1. Create IAM role with Bedrock permissions
2. Attach to Render service/instance
3. Remove AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY from environment

### 4. Build and Deploy
```bash
mvn clean package
# Render will automatically deploy with Dockerfile
```

## Features

### 1. Automatic Policy Number Extraction
- Regex pattern matching for policy numbers (POL-XXXXX, Policy#XXXXXXXX)
- Integrated into agent response processing

### 2. Mock Agent Processing
- Simulates agent response for development
- Includes claim status, policy details, attachment count
- Extendable for real Bedrock Agent integration

### 3. Session Management
- Automatic session ID generation if not provided
- Session state variables support
- Trace events for debugging

### 4. Request Metadata Tracking
- Processing time measurement
- Agent response metadata
- Error details capture

## Swagger/OpenAPI Documentation

The application includes Swagger documentation:
- Access at: `http://localhost:8080/swagger-ui.html`
- API Docs at: `http://localhost:8080/v3/api-docs`

All new endpoints are documented with descriptions and examples.

## Testing

### Local Testing
```bash
# Test FNOL email processing
curl -X POST http://localhost:8080/fnol/email \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Test Claim",
    "body": "Policy POL-12345 claim test",
    "sender": "test@example.com",
    "recipients": ["claims@company.com"],
    "queueId": "test-001",
    "timestamp": "2026-06-05T12:00:00Z",
    "attachmentPaths": ["/path/to/doc.pdf"]
  }'

# Get agent details
curl http://localhost:8080/bedrock-agent/details

# Health check
curl http://localhost:8080/bedrock-agent/health
```

## Error Handling

### Common Errors

**Error: "Unable to load credentials"**
- Solution: Set AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY or configure AWS CLI
- Or attach IAM role to Render instance

**Error: "Agent not found"**
- Verify aws.bedrock.agent.id is correct
- Check agent exists in Bedrock console

**Error: "Invoking Bedrock Agent failed"**
- Check AWS region is correct
- Verify IAM permissions for Bedrock APIs
- Check agent alias ID is active

## Future Enhancements

1. **Real Bedrock Agent Integration**
   - Replace simulate function with actual AWS Bedrock API calls
   - Update with correct InvokeAgent request/response handling

2. **Knowledge Base Integration**
   - Connect to Bedrock Knowledge Bases for RAG
   - Enhanced document processing

3. **Multi-Agent Support**
   - Support multiple agents for different claim types
   - Agent routing logic

4. **Response Caching**
   - Cache agent responses for similar emails
   - Reduce API calls

5. **Async Processing**
   - Use SQS/SNS for async processing
   - Long-running claim analysis

## References

- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Springdoc OpenAPI](https://springdoc.org/)

