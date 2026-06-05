# Quick Start Guide - Bedrock Agent Integration

## For Developers

### 1. Build the Project
```bash
mvn clean package -DskipTests
```

### 2. Run Locally
```bash
# With default mock agent
java -jar target/policysubmission-0.0.1-SNAPSHOT.jar

# Or with custom environment variables
java -jar target/policysubmission-0.0.1-SNAPSHOT.jar \
  -Daws.bedrock.region=us-east-1 \
  -Daws.bedrock.agent.id=your-agent-id
```

### 3. Test the API
```bash
# FNOL Email endpoint
curl -X POST http://localhost:8080/fnol/email \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Vehicle Claim",
    "body": "I have a claim for policy POL-12345",
    "sender": "customer@example.com",
    "recipients": ["claims@company.com"],
    "queueId": "test-001",
    "timestamp": "2026-06-05T12:00:00Z",
    "attachmentPaths": ["/docs/report.pdf"]
  }'

# View Swagger documentation
open http://localhost:8080/swagger-ui.html
```

### 4. Check Agent Details
```bash
curl http://localhost:8080/bedrock-agent/details
```

## For Deployment Engineers

### 1. Deploy to Render

```bash
# Prerequisites
- Render account
- Docker support

# Steps
1. Push code to GitHub
2. Connect Render to repository
3. Set environment variables:
   - AWS_REGION=us-east-1
   - AWS_BEDROCK_AGENT_ID=your-id
   - AWS_BEDROCK_AGENT_ALIAS_ID=your-alias
4. Deploy using Dockerfile
```

### 2. Configure AWS Credentials (Choose One)

**Option A: IAM Role (Recommended)**
```bash
# Attach IAM role to instance
No environment variables needed
```

**Option B: Environment Variables**
```bash
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret
AWS_REGION=us-east-1
```

**Option C: AWS Credentials File**
```bash
Mount at: /home/ubuntu/.aws/credentials
```

### 3. Verify Deployment
```bash
curl https://YOUR_SERVICE.onrender.com/bedrock-agent/health
```

## For System Architects

### Key Components

1. **BedrockConfig** - AWS SDK initialization
2. **BedrockAgentService** - Core business logic
3. **BedrockAgentController** - REST API endpoints
4. **EmailController** - FNOL email processing
5. **DTOs** - Request/response modeling

### Flow Diagram
```
Email Received
    ↓
EmailController validates
    ↓
BedrockAgentService formats request
    ↓
Invokes Bedrock Agent (or simulates)
    ↓
Extracts policy numbers
    ↓
Returns structured response
```

### API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | /fnol/email | Process FNOL email |
| POST | /claims-fnol/email | Process claims email |
| POST | /bedrock-agent/invoke | Direct agent invocation |
| GET | /bedrock-agent/details | Get agent configuration |
| GET | /bedrock-agent/health | Health check |

## For Operations

### Monitoring
- Monitor logs in Render dashboard
- Check /bedrock-agent/health endpoint
- Track API response times
- Monitor error rates

### Scaling
- More instances needed? Upgrade Render plan
- Storage issues? Check attachment paths
- Rate limiting? Implement request throttling

### Troubleshooting

**Issue: "Unable to load credentials"**
→ Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY

**Issue: "Agent not found"**
→ Verify AWS_BEDROCK_AGENT_ID is correct

**Issue: Timeout errors**
→ Upgrade Render instance size

## File Structure
```
src/main/java/com/vm/service/policysubmission/
├── config/
│   └── BedrockConfig.java          (AWS Bedrock client setup)
├── controller/
│   ├── BedrockAgentController.java (Agent management API)
│   ├── EmailController.java         (FNOL API)
│   └── EmailReceiveController.java  (Claims FNOL API)
├── service/
│   ├── BedrockAgentService.java     (Agent invocation logic)
│   └── EmailReceiveService.java     (Email processing)
└── dto/
    ├── BedrockAgentRequest.java     (Request model)
    └── BedrockAgentResponse.java    (Response model)

Documentation/
├── BEDROCK_INTEGRATION.md           (Complete guide)
├── RENDER_DEPLOYMENT_GUIDE.md       (Render instructions)
├── BEDROCK_TEST_EXAMPLES.md         (Test examples)
└── IMPLEMENTATION_SUMMARY.md        (This summary)
```

## Common Tasks

### Task: Deploy New Version
```bash
1. Update code
2. Run: mvn clean package -DskipTests
3. Commit to git
4. Push to branch
5. Render auto-deploys
6. Verify with: curl https://YOUR_SERVICE.onrender.com/bedrock-agent/health
```

### Task: Update Agent Configuration
```bash
1. Go to Render Dashboard
2. Select service
3. Environment tab
4. Update AWS_BEDROCK_AGENT_ID or AWS_BEDROCK_AGENT_ALIAS_ID
5. Click Save & Redeploy
```

### Task: Monitor Logs
```bash
1. Open Render Dashboard
2. Navigate to Logs section
3. Search for "Bedrock" or "ERROR"
4. Check response times
```

### Task: Test New Email Format
```bash
1. Prepare JSON request file (request.json)
2. Run: curl -X POST http://localhost:8080/fnol/email -H "Content-Type: application/json" -d @request.json
3. Verify response contains: sessionId, agentStatus, bedrockAgentResponse
```

## Security Reminders

- ⚠️ Never commit credentials to git
- ⚠️ Always use environment variables for secrets
- ⚠️ Rotate access keys regularly (if using them)
- ⚠️ Use IAM roles instead of static credentials
- ⚠️ Validate all input data
- ⚠️ Enable HTTPS (automatic on Render)

## Support Resources

- **AWS Bedrock Docs:** https://docs.aws.amazon.com/bedrock/
- **Render Docs:** https://render.com/docs
- **Application Logs:** Render Dashboard → Logs
- **API Docs:** http://localhost:8080/swagger-ui.html

## Version Info

- Java: 17
- Spring Boot: 3.5.7
- AWS SDK: 2.25.46
- Build Status: ✅ SUCCESS

## Next Steps

1. ✅ Build the project locally
2. ✅ Test the email endpoints
3. ✅ Review Swagger documentation
4. ✅ Deploy to Render
5. ✅ Configure AWS credentials
6. ✅ Set up monitoring/alerts
7. ✅ Integrate with real Bedrock Agent

