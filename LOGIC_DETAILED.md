# 🧠 Internal Logic & Implementation Guide

This document explains the technical implementation of the Cloud Posture Scanner, including file responsibilities and the logic behind security checks.

## 📂 File-by-File Responsibility

### 1. Controllers
*   **`CloudPostureController.java`**: 
    *   Exposes endpoints like `/api/scan`, `/api/instances`, `/api/buckets`.
    *   Handles incoming HTTP requests and maps them to service layer calls.
    *   Formulates the dashboard summary by aggregating data from the database.

### 2. Services (The Brain)
*   **`ScanService.java`**: 
    *   The **Orchestrator**. 
    *   Coordinates the flow: `Create Tables` -> `Discover EC2` -> `Discover S3` -> `Run CIS Checks` -> `Store Results`.
*   **`EC2DiscoveryService.java`**: 
    *   Uses `Ec2Client` to fetch all instances across the region.
    *   Extracts instance ID, type, public IP, and security group IDs.
*   **`S3DiscoveryService.java`**: 
    *   Uses `S3Client` to list all buckets.
    *   Performs follow-up calls for each bucket to check:
        *   `GetBucketEncryption`: Is default encryption ON?
        *   `GetPublicAccessBlock`: Is "Block All Public Access" ON?
*   **`CISBenchmarkService.java`**: 
    *   Contains the core security auditing logic. (Detailed below).
*   **`DynamoDbStorageService.java`**: 
    *   Handles all interactions with AWS DynamoDB.
    *   Automatically creates 3 tables: `CloudPosture_EC2Instances`, `CloudPosture_S3Buckets`, and `CloudPosture_CISResults`.

---

## 🛠️ How CIS Checks Are Implemented

### 1. S3 Public Access (CIS 2.1.5)
*   **Logic**: High Severity. 
*   **How**: We check the `PublicAccessBlock` configuration of every bucket. If `BlockPublicAcl` or `BlockPublicPolicy` is false, it's flagged as an "unsecured" bucket.
*   **Pass Condition**: Every bucket has "Block all public access" enabled.

### 2. S3 Encryption (CIS 2.1.1)
*   **Logic**: Medium Severity.
*   **How**: For each bucket, we attempt to retrieve the `ServerSideEncryptionConfiguration`. If the AWS SDK returns a `404 Not Found` or the list is empty, it means the bucket is unencrypted.
*   **Pass Condition**: Every bucket has at least AES-256 (SSE-S3) encryption.

### 3. IAM Root MFA (CIS 1.5)
*   **Logic**: Critical Severity.
*   **How**: We call `iamClient.getAccountSummary()`. This returns a map of "Account Summary" metrics. We specifically look for the key **`AccountMFAEnabled`**.
*   **Pass Condition**: The value returned by AWS for this key is greater than `0`.

### 4. CloudTrail Enabled (CIS 3.1)
*   **Logic**: High Severity.
*   **How**: We call `cloudTrailClient.describeTrails()`. 
*   **Pass Condition**: At least one trail exists in the account. (Ideally, it should be multi-region, but the tool checks for basic presence).

### 5. Restricted Security Groups (CIS 5.2)
*   **Logic**: High Severity.
*   **How**: We loop through all Security Groups and their `IpPermissions`.
    *   We look for rules where the `FromPort` contains **22** (SSH) or **3389** (RDP).
    *   We then check the `IpRanges`. If any range is **`0.0.0.0/0`** (the whole world), it's a security failure.
*   **Pass Condition**: No SG allows open port 22 or 3389 to the public internet.

---

## 🖥️ Manual Environment Setup (Windows/OS)

If you don't want to use a `.env` file, you can set these manually in your Operating System:

### Path A: Windows UI (Permanent)
1.  Search for **"Edit the system environment variables"** in the Start menu.
2.  Click the **"Environment Variables"** button.
3.  Under **"User variables"**, click **New**.
4.  Add:
    *   Name: `AWS_ACCESS_KEY_ID` | Value: `(Your Key)`
    *   Name: `AWS_SECRET_ACCESS_KEY` | Value: `(Your Secret)`
    *   Name: `AWS_DEFAULT_REGION` | Value: `us-east-1`
5.  **Restart your IDE** (VS Code / IntelliJ) for changes to take effect.

### Path B: Windows PowerShell (Temporary for current session)
Run these commands before starting the app:
```powershell
$env:AWS_ACCESS_KEY_ID="AKIA..."
$env:AWS_SECRET_ACCESS_KEY="wJalr..."
$env:AWS_DEFAULT_REGION="us-east-1"
```

### Path C: application.yml (Direct Hardcode - NOT RECOMMENDED)
You can technically put them in `backend/src/main/resources/application.yml` directly, but this is dangerous because you might accidentally push them to GitHub.
```yaml
aws:
  access-key: YOUR_KEY
  secret-key: YOUR_SECRET
```
*(Our current code is set to look for environment variables first, which is the industry standard for security).*
