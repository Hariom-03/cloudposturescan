 # Cloud Posture Scanner

<div align="center">

🛡️ **AWS Cloud Security Posture Management Tool**

A lightweight security assessment tool that discovers AWS resources and performs CIS AWS Benchmark compliance checks.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen)
![React](https://img.shields.io/badge/React-18.2-blue)
![AWS](https://img.shields.io/badge/AWS-SDK%20v2-yellow)

</div>

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [API Documentation](#-api-documentation)
- [CIS Checks Implemented](#-cis-checks-implemented)
- [Demo Guide](#-demo-guide)
- [Screenshots](#-screenshots)

## ✨ Features

### Resource Discovery
- **EC2 Instances**: Discovers all instances with instance ID, type, region, public IP, and security groups
- **S3 Buckets**: Lists all buckets with encryption status, access policies, and security configurations

### CIS AWS Benchmark Checks (5 Implemented)
1. ✅ **CIS 2.1.5** - Ensure S3 buckets are not publicly accessible
2. ✅ **CIS 2.1.1** - Ensure S3 bucket encryption is enabled
3. ✅ **CIS 1.5** - Ensure IAM root account has MFA enabled
4. ✅ **CIS 3.1** - Ensure CloudTrail is enabled in all regions
5. ✅ **CIS 5.2** - Ensure security groups don't allow unrestricted SSH/RDP access

### Data Storage
- Secure storage of scan results in AWS DynamoDB
- Automatic table creation with on-demand billing
- Historical tracking of all scans

### REST APIs
- `POST /api/scan` - Trigger new security scan
- `GET /api/instances` - Retrieve EC2 instances
- `GET /api/buckets` - Retrieve S3 buckets
- `GET /api/cis-results` - Retrieve CIS check results
- `GET /api/dashboard/summary` - Get dashboard summary metrics

### Frontend Dashboard
- Real-time security posture visualization
- Interactive tables for EC2 and S3 resources
- CIS compliance results with pass/fail indicators
- Compliance rate calculation
- One-click scan triggering

## 🏗️ Architecture

```
┌─────────────────┐
│  React Frontend │
│   (Port 5173)   │
└────────┬────────┘
         │ HTTP/REST
         ▼
┌─────────────────────────┐
│  Spring Boot Backend    │
│      (Port 8080)        │
│  ┌──────────────────┐   │
│  │ REST Controllers │   │
│  └─────────┬────────┘   │
│            │            │
│  ┌─────────▼────────┐   │
│  │  Scan Service    │   │
│  └─────────┬────────┘   │
│            │            │
│  ┌─────────┴──────────────────────┐
│  │                                │
│  ▼                                ▼
│  ┌──────────────┐    ┌──────────────────┐
│  │  Discovery   │    │  CIS Benchmark   │
│  │  Services    │    │    Service       │
│  └──────┬───────┘    └────────┬─────────┘
│         │                     │
└─────────┼─────────────────────┼──────────┘
          │                     │
          ▼                     ▼
    ┌─────────────────────────────────┐
    │         AWS Services            │
    │  ┌────┐ ┌────┐ ┌────┐ ┌──────┐  │ 
    │  │EC2 │ │ S3 │ │IAM │ │Cloud │  | 
    │  │    │ │    │ │    │ │Trail │  │
    │  └────┘ └────┘ └────┘ └──────┘  │
    │                                 │
    │         ┌──────────┐            │
    │         │ DynamoDB │ (Storage)  │
    │         └──────────┘            │
    └─────────────────────────────────┘
```

## 📋 Prerequisites

### Required Software
- **Java 17** or higher
- **Maven 3.6+**
- **Node.js 18+** and npm
- **AWS Account** (Free tier is sufficient)

### AWS Requirements
1. **AWS Account**: Active AWS account
2. **IAM User** with programmatic access (Access Key ID + Secret Access Key)
3. **IAM Permissions**: The IAM user needs the following permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:DescribeInstances",
        "ec2:DescribeSecurityGroups",
        "s3:ListAllMyBuckets",
        "s3:GetBucketLocation",
        "s3:GetBucketEncryption",
        "s3:GetBucketPolicyStatus",
        "s3:GetPublicAccessBlock",
        "s3:GetBucketVersioning",
        "iam:GetAccountSummary",
        "iam:ListVirtualMFADevices",
        "cloudtrail:DescribeTrails",
        "dynamodb:CreateTable",
        "dynamodb:DescribeTable",
        "dynamodb:PutItem",
        "dynamodb:Scan"
      ],
      "Resource": "*"
    }
  ]
}
```

## 🚀 Installation

### 1. Clone the Repository
```bash
cd d:\desktop\visiblaze
```

### 2. Configure AWS Credentials

**Option A: Environment Variables (Recommended)**
```bash
# Windows PowerShell
$env:AWS_ACCESS_KEY_ID="your-access-key-id"
$env:AWS_SECRET_ACCESS_KEY="your-secret-access-key"
$env:AWS_DEFAULT_REGION="us-east-1"
```

**Option B: AWS Credentials File**
Create/edit `~/.aws/credentials`:
```ini
[default]
aws_access_key_id = your-access-key-id
aws_secret_access_key = your-secret-access-key
```

Create/edit `~/.aws/config`:
```ini
[default]
region = us-east-1
```

### 3. Install Backend Dependencies
```bash
cd backend
mvn clean install
```

### 4. Install Frontend Dependencies
```bash
cd ../frontend
npm install
```

## ⚙️ Configuration

### Backend Configuration
Edit `backend/src/main/resources/application.yml`:

```yaml
aws:
  region: us-east-1  # Change to your preferred region
  dynamodb:
    table:
      ec2-instances: CloudPosture_EC2Instances
      s3-buckets: CloudPosture_S3Buckets
      cis-results: CloudPosture_CISResults
```

### Frontend Configuration
The frontend automatically connects to `http://localhost:8080` for the backend API.

## 🎯 Running the Application

### Start Backend (Terminal 1)
```bash
cd backend
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

### Start Frontend (Terminal 2)
```bash
cd frontend
npm run dev
```

The frontend will start on **http://localhost:5173**

### Access the Dashboard
Open your browser and navigate to: **http://localhost:5173**

## 📡 API Documentation

### Trigger Security Scan
```http
POST /api/scan
```

**Response:**
```json
{
  "scanId": "uuid",
  "status": "COMPLETED",
  "startTime": 1234567890,
  "endTime": 1234567900,
  "ec2InstancesFound": 5,
  "s3BucketsFound": 10,
  "checksPerformed": 5,
  "checksPassed": 3,
  "checksFailed": 2,
  "errors": []
}
```

### Get EC2 Instances
```http
GET /api/instances
```

**Response:**
```json
[
  {
    "instanceId": "i-1234567890abcdef0",
    "instanceType": "t2.micro",
    "region": "us-east-1",
    "publicIp": "54.123.45.67",
    "privateIp": "10.0.1.10",
    "state": "RUNNING",
    "securityGroups": ["sg-12345678"],
    "availabilityZone": "us-east-1a",
    "launchTime": "2024-01-01T10:00:00Z",
    "scanTimestamp": 1234567890
  }
]
```

### Get S3 Buckets
```http
GET /api/buckets
```

**Response:**
```json
[
  {
    "bucketName": "my-bucket",
    "region": "us-east-1",
    "encryptionEnabled": true,
    "encryptionType": "AES256",
    "accessPolicy": "PRIVATE",
    "blockPublicAccess": true,
    "versioningEnabled": false,
    "creationDate": "2024-01-01T10:00:00Z",
    "scanTimestamp": 1234567890
  }
]
```

### Get CIS Results
```http
GET /api/cis-results
```

**Response:**
```json
[
  {
    "checkId": "CIS-2.1.5",
    "checkName": "S3 Buckets Not Publicly Accessible",
    "description": "Ensure that S3 buckets are not publicly accessible",
    "status": "PASS",
    "evidence": "All 10 S3 buckets are private",
    "recommendation": "N/A",
    "severity": "HIGH",
    "scanTimestamp": 1234567890
  }
]
```

### Get Dashboard Summary
```http
GET /api/dashboard/summary
```

**Response:**
```json
{
  "totalEC2Instances": 5,
  "totalS3Buckets": 10,
  "totalCISChecks": 5,
  "checksPassedCount": 3,
  "checksFailedCount": 2,
  "complianceRate": 60
}
```

## 🔍 CIS Checks Implemented

### 1. CIS 2.1.5 - S3 Buckets Not Publicly Accessible
**Severity:** HIGH  
**Checks:** Verifies that no S3 buckets have public access enabled  
**Evidence:** Lists all public buckets found  
**Recommendation:** Enable S3 Block Public Access for all buckets

### 2. CIS 2.1.1 - S3 Bucket Encryption Enabled
**Severity:** MEDIUM  
**Checks:** Verifies all S3 buckets have default encryption enabled  
**Evidence:** Lists all unencrypted buckets  
**Recommendation:** Enable AES-256 or AWS-KMS encryption for all buckets

### 3. CIS 1.5 - IAM Root Account MFA Enabled
**Severity:** HIGH  
**Checks:** Verifies that MFA is enabled for the root account  
**Evidence:** MFA enabled/disabled status  
**Recommendation:** Enable virtual or hardware MFA for root account

### 4. CIS 3.1 - CloudTrail Enabled
**Severity:** HIGH  
**Checks:** Verifies that CloudTrail is enabled in all regions  
**Evidence:** Number of CloudTrail trails configured  
**Recommendation:** Enable CloudTrail for audit logging

### 5. CIS 5.2 - Security Groups Restricted Access
**Severity:** HIGH  
**Checks:** Ensures no security groups allow 0.0.0.0/0 for SSH (22) or RDP (3389)  
**Evidence:** Lists security groups with unrestricted access  
**Recommendation:** Restrict to specific IP addresses

## 🎥 Demo Guide

When presenting this project, demonstrate:

### 1. How Discovery Works
- Click "Run Security Scan" button
- Show real-time scanning process
- Display discovered EC2 instances and S3 buckets in tables

### 2. CIS Checks Implementation
- Explain each of the 5 CIS checks
- Show pass/fail results with evidence
- Highlight severity levels

### 3. Data Flow to AWS
- Show DynamoDB tables being created
- Demonstrate data persistence across page refreshes
- Explain the storage architecture

### 4. API and Frontend Outputs
- Use browser DevTools to show API calls
- Demonstrate the REST endpoints with Postman/curl
- Show the responsive dashboard UI

### 5. Design Decisions
**Backend:**
- Spring Boot for enterprise-grade Java framework
- AWS SDK v2 for modern AWS integration
- Modular service architecture for maintainability

**Frontend:**
- React with Vite for fast development
- Premium dark theme for professional appearance
- Component-based architecture

**Storage:**
- DynamoDB for serverless, scalable storage
- Pay-per-request billing for cost efficiency
- Automatic table management

### 6. Potential Improvements
- Add more CIS benchmark checks (200+ available)
- Implement scheduled scans with Lambda
- Add email notifications for failed checks
- Multi-region support
- Historical trend analysis
- Export reports to PDF
- Integration with AWS Security Hub
- Role-based access control
- Real-time monitoring with WebSockets

## 📸 Screenshots

*The dashboard features:*
- **Dashboard Overview**: Key metrics and compliance rate
- **CIS Results**: Pass/fail status with evidence and recommendations
- **EC2 Instances Table**: Instance details with security groups
- **S3 Buckets Table**: Encryption and access policy status

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.1
- **Frontend**: React 18, Vite 5
- **Cloud**: AWS SDK v2 (EC2, S3, IAM, CloudTrail, DynamoDB)
- **Build Tools**: Maven, npm
- **Styling**: Pure CSS with modern design system

## 📝 License

This project is built as an assignment demonstration.

## 👤 Author

Built for cloud security posture assessment assignment.

---

<div align="center">

**🛡️ Cloud Posture Scanner - Securing AWS Infrastructure 🛡️**

</div>
