# Render Deployment Guide for Bedrock Agent Integration

## Prerequisites
- Render account with paid/free plan
- AWS account with Bedrock access
- Docker images built locally or pushed to registry

## Environment Variables for Render

Add these environment variables in Render Dashboard > Environment:

```bash
# AWS Region
AWS_REGION=us-east-1

# Bedrock Agent Configuration (Mock for testing)
AWS_BEDROCK_AGENT_ID=MOCK-AGENT-ID
AWS_BEDROCK_AGENT_ALIAS_ID=LFZQYFUWQZ

# AWS Authentication (Choose ONE approach below)
```

## Option 1: Using IAM Role with IAM Anywhere (Recommended for EC2)

If deploying to Render on EC2 with IAM role:
1. Attach IAM role with Bedrock permissions to Render instance
2. Do NOT set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
3. Application will automatically use instance credentials

## Option 2: Using Environment Variables (Temporary Credentials)

```bash
# For rotating/temporary credentials
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY

# Optional: For session tokens (STS)
AWS_SESSION_TOKEN=FwoGZXIvYXdzEhP...
```

## Option 3: Using AWS_PROFILE and Credentials File

Mount AWS credentials file and set:
```bash
AWS_PROFILE=default
```

## Step-by-Step Deployment

### 1. Build Docker Image Locally

```bash
cd vm-service-policy-submission
mvn clean package -DskipTests
docker build -t vm-service-policy-submission:latest .
```

### 2. Push to Container Registry

#### Option A: Docker Hub
```bash
docker tag vm-service-policy-submission:latest YOUR_DOCKER_HUB/vm-service-policy-submission:latest
docker push YOUR_DOCKER_HUB/vm-service-policy-submission:latest
```

#### Option B: Use Render's native registry
```bash
# Render will handle building from Dockerfile
```

### 3. Create/Update Service in Render

1. Go to Render Dashboard > New > Web Service
2. Connect GitHub repository
3. Select branch to deploy
4. Configure:
   - Runtime: Docker
   - Build Command: Leave default
   - Start Command: Leave default
   - Instance Type: Free/Starter (adjust based on needs)
   - Region: Virginia (US-East)

### 4. Set Environment Variables in Render

In Render dashboard under Environment:

```
AWS_REGION=us-east-1
AWS_BEDROCK_AGENT_ID=MOCK-AGENT-ID
AWS_BEDROCK_AGENT_ALIAS_ID=LFZQYFUWQZ
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
```

### 5. Update Dockerfile

Ensure Dockerfile includes:
```dockerfile
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY target/policysubmission-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
```

### 6. Deploy

Render will automatically deploy when:
- You push to the configured branch
- Or click "Deploy" in Render dashboard

## Verifying Deployment

### 1. Check Service Logs
```bash
# In Render dashboard > Logs
# Look for application startup
```

### 2. Test Health Endpoint
```bash
curl https://YOUR_SERVICE.onrender.com/bedrock-agent/health
```

### 3. Test FNOL Email Endpoint
```bash
curl -X POST https://YOUR_SERVICE.onrender.com/fnol/email \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Test Claim",
    "body": "Policy POL-12345 claim test",
    "sender": "test@example.com",
    "recipients": ["claims@company.com"],
    "queueId": "test-001",
    "timestamp": "2026-06-05T12:00:00Z",
    "attachmentPaths": []
  }'
```

## Troubleshooting

### Issue: "Unable to load credentials from any of the providers"

**Cause**: AWS credentials not configured

**Solution**:
1. Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY in Render environment
2. Or use IAM role if on EC2
3. Check credentials have Bedrock permissions: `bedrock:InvokeAgent`

### Issue: "Agent not found"

**Cause**: Agent ID or alias ID incorrect

**Solution**:
1. Verify agent exists in AWS Bedrock console
2. Confirm agent is in same region (AWS_REGION)
3. Update AWS_BEDROCK_AGENT_ID and AWS_BEDROCK_AGENT_ALIAS_ID

### Issue: 403 Forbidden or Access Denied

**Cause**: IAM permissions insufficient

**Solution**:
1. Attach policy with Bedrock permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeAgent",
        "bedrock:GetAgent",
        "bedrock:ListAgents"
      ],
      "Resource": "*"
    }
  ]
}
```

### Issue: Slow responses or timeouts

**Cause**: Instance too small or Bedrock region latency

**Solution**:
1. Upgrade Render plan to Standard/Pro
2. Increase timeout values in application.properties
3. Use region closer to your data

## Monitoring and Maintenance

### Log Rotation
Render automatically rotates logs. Access via:
- Render Dashboard > Logs
- Or use Render CLI: `render logs`

### Performance Metrics
Monitor in Render:
- CPU usage
- Memory usage
- Request latency
- Error rates

### Auto-scaling
Configure in Render under Plan:
- Min instances
- Max instances
- Scale up/down thresholds

## Production Checklist

- [ ] Update AWS_BEDROCK_AGENT_ID with real agent ID
- [ ] Update AWS_BEDROCK_AGENT_ALIAS_ID with real alias
- [ ] Use IAM role instead of static credentials
- [ ] Enable encryption for credentials in Render
- [ ] Configure log retention period
- [ ] Set up alerts for errors
- [ ] Test failover scenarios
- [ ] Document runbooks for incidents
- [ ] Set up monitoring/observability
- [ ] Validate email attachment processing
- [ ] Test large payload handling

## Updating Application Code

### Deploy Changes
1. Update code locally
2. Commit to git: `git commit -m "Update Bedrock integration"`
3. Push to branch: `git push origin main`
4. Render auto-deploys (if auto-deploy enabled)

### Manual Deploy
1. Go to Render Dashboard > Services
2. Select service
3. Click "Manual Deploy" > "Deploy latest"

## Scaling Considerations

### For High Volume
1. Upgrade to Pro plan
2. Configure multiple instances
3. Set up load balancer
4. Consider async processing with queues
5. Implement response caching

### Cost Optimization
1. Use appropriate instance size
2. Set auto-scaling limits
3. Monitor Bedrock API usage (charged per invocation)
4. Cache responses where applicable

## Security Best Practices

1. **Never commit credentials** to git
2. **Use environment variables** for secrets
3. **Rotate access keys** regularly
4. **Use IAM roles** instead of access keys
5. **Enable HTTPS** (default in Render)
6. **Validate email inputs** for security
7. **Log sensitive data** appropriately
8. **Use VPN** if needed for private connections

## Support

For Render issues: https://render.com/docs
For AWS Bedrock issues: https://aws.amazon.com/bedrock/support
For Application issues: Check application logs in Render dashboard

