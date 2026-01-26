# Deploying to Render - Complete Guide

## Overview
This guide covers deploying the Digital Wallet Platform to Render using GitHub Actions for CI/CD automation.

---

## Prerequisites

1. **Render Account**: Sign up at [render.com](https://render.com)
2. **GitHub Repository**: Code pushed to GitHub
3. **Render API Key**: Generate from Render Dashboard → Account Settings → API Keys

---

## Step 1: Create Render Services

### Option A: Using render.yaml (Recommended)

1. **Connect GitHub Repository** to Render
2. **Create Blueprint** from `render.yaml`
3. Render will automatically create:
   - Web Service (Spring Boot app)
   - PostgreSQL Database
   - Redis Instance

### Option B: Manual Setup

#### Create PostgreSQL Database
1. Dashboard → New → PostgreSQL
2. Name: `digital-wallet-db`
3. Database: `digital_wallet_db`
4. Plan: Starter ($7/month)

#### Create Redis Instance
1. Dashboard → New → Redis
2. Name: `digital-wallet-redis`
3. Plan: Starter ($10/month)

#### Create Web Service
1. Dashboard → New → Web Service
2. Connect GitHub repository
3. Settings:
   - **Name**: `digital-wallet-platform`
   - **Environment**: Docker
   - **Region**: Oregon (or nearest)
   - **Branch**: `main`
   - **Dockerfile Path**: `./Dockerfile`
   - **Plan**: Starter ($7/month)

---

## Step 2: Configure Environment Variables

In Render Web Service → Environment:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=<from database connection string>
SPRING_DATASOURCE_USERNAME=<from database>
SPRING_DATASOURCE_PASSWORD=<from database>
SPRING_DATA_REDIS_HOST=<from redis instance>
SPRING_DATA_REDIS_PORT=6379
SPRING_CLOUD_AWS_REGION_STATIC=us-east-1
SPRING_CLOUD_AWS_CREDENTIALS_ACCESS_KEY=<your-aws-key>
SPRING_CLOUD_AWS_CREDENTIALS_SECRET_KEY=<your-aws-secret>
JWT_SECRET=<generate-random-base64-string>
JWT_EXPIRATION=86400000
```

**Generate JWT Secret**:
```bash
openssl rand -base64 64
```

---

## Step 3: Configure GitHub Secrets

Add these secrets to your GitHub repository (Settings → Secrets → Actions):

```
RENDER_API_KEY=<your-render-api-key>
RENDER_STAGING_SERVICE_ID=<staging-service-id>
RENDER_PRODUCTION_SERVICE_ID=<production-service-id>
```

**Find Service ID**:
- Render Dashboard → Service → Settings → Service ID

---

## Step 4: Deploy

### Automatic Deployment (via GitHub Actions)

**Staging Deployment**:
```bash
git push origin main
# Automatically triggers staging deployment
```

**Production Deployment**:
```bash
git tag v1.0.0
git push origin v1.0.0
# Triggers production deployment with approval gate
```

### Manual Deployment

1. Render Dashboard → Service → Manual Deploy
2. Select branch/commit
3. Click "Deploy"

---

## Step 5: Verify Deployment

### Health Check
```bash
curl https://digital-wallet-platform.onrender.com/actuator/health
```

### Test API
```bash
# Register user
curl -X POST https://digital-wallet-platform.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "1234567890"
  }'
```

---

## CI/CD Pipeline Flow

```
┌─────────────┐
│ Push to main│
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ CI Workflow     │
│ - Code Quality  │
│ - Security Scan │
│ - Tests         │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Deploy Staging  │
│ (Automatic)     │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Health Check    │
└──────┬──────────┘
       │
       ▼ (on tag)
┌─────────────────┐
│ Approval Gate   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ Deploy Prod     │
└─────────────────┘
```

---

## Monitoring

### Render Dashboard
- **Logs**: Real-time application logs
- **Metrics**: CPU, Memory, Request rate
- **Events**: Deployment history

### Application Metrics
- **Prometheus**: `https://your-app.onrender.com/actuator/prometheus`
- **Health**: `https://your-app.onrender.com/actuator/health`

---

## Troubleshooting

### Build Fails
```bash
# Check Render logs
# Common issues:
# 1. Missing environment variables
# 2. Database connection timeout
# 3. Docker build errors
```

### Database Connection Issues
- Verify `SPRING_DATASOURCE_URL` format: `jdbc:postgresql://host:port/database`
- Check database is in same region as web service
- Ensure database is running (not suspended)

### Redis Connection Issues
- Verify Redis host and port
- Check Redis instance status
- Ensure Redis is in same region

---

## Cost Estimate

| Service | Plan | Monthly Cost |
|---------|------|--------------|
| Web Service | Starter | $7 |
| PostgreSQL | Starter | $7 |
| Redis | Starter | $10 |
| **Total** | | **$24/month** |

**Free Tier**: Render offers free tier for testing (with limitations)

---

## Scaling

### Horizontal Scaling
Render Dashboard → Service → Settings → Instance Count

### Vertical Scaling
Upgrade to higher plan (Standard, Pro, etc.)

---

## Rollback

```bash
# Via Render Dashboard
1. Service → Deploys
2. Find previous successful deploy
3. Click "Rollback"

# Via GitHub
git revert <commit-hash>
git push origin main
```
