package com.visiblaze.service.benchmark;

import com.visiblaze.model.CISCheckResult;
import com.visiblaze.model.S3BucketInfo;
import com.visiblaze.service.discovery.EC2DiscoveryService;
import com.visiblaze.service.discovery.S3DiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.model.DescribeTrailsRequest;
import software.amazon.awssdk.services.cloudtrail.model.DescribeTrailsResponse;
import software.amazon.awssdk.services.cloudtrail.model.Trail;
import software.amazon.awssdk.services.ec2.model.IpPermission;
import software.amazon.awssdk.services.ec2.model.IpRange;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.GetAccountSummaryRequest;
import software.amazon.awssdk.services.iam.model.GetAccountSummaryResponse;
import software.amazon.awssdk.services.iam.model.ListVirtualMfaDevicesRequest;
import software.amazon.awssdk.services.iam.model.ListVirtualMfaDevicesResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CISBenchmarkService {

    private final S3DiscoveryService s3DiscoveryService;
    private final EC2DiscoveryService ec2DiscoveryService;
    private final IamClient iamClient;
    private final CloudTrailClient cloudTrailClient;

    public List<CISCheckResult> runAllChecks() {
        log.info("Starting CIS benchmark checks...");
        List<CISCheckResult> results = new ArrayList<>();

        results.add(checkS3BucketsNotPublic());
        results.add(checkS3BucketsEncrypted());
        results.add(checkIAMRootMFAEnabled());
        results.add(checkCloudTrailEnabled());
        results.add(checkSecurityGroupsNotOpenToWorld());

        log.info("Completed {} CIS benchmark checks", results.size());
        return results;
    }

    /**
     * CIS Check 1: Ensure no S3 buckets are publicly accessible
     */
    private CISCheckResult checkS3BucketsNotPublic() {
        log.info("Running CIS Check: S3 Buckets Not Public");
        try {
            List<S3BucketInfo> buckets = s3DiscoveryService.discoverBuckets();
            List<String> publicBuckets = new ArrayList<>();

            for (S3BucketInfo bucket : buckets) {
                if ("PUBLIC".equals(bucket.getAccessPolicy()) || !bucket.isBlockPublicAccess()) {
                    publicBuckets.add(bucket.getBucketName());
                }
            }

            if (publicBuckets.isEmpty()) {
                return CISCheckResult.builder()
                        .checkId("CIS-2.1.5")
                        .checkName("S3 Buckets Not Publicly Accessible")
                        .description("Ensure that S3 buckets are not publicly accessible")
                        .status("PASS")
                        .evidence(String.format("All %d S3 buckets are private", buckets.size()))
                        .recommendation("N/A")
                        .severity("HIGH")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            } else {
                return CISCheckResult.builder()
                        .checkId("CIS-2.1.5")
                        .checkName("S3 Buckets Not Publicly Accessible")
                        .description("Ensure that S3 buckets are not publicly accessible")
                        .status("FAIL")
                        .evidence(String.format("Found %d public buckets: %s",
                                publicBuckets.size(), String.join(", ", publicBuckets)))
                        .recommendation(
                                "Enable S3 Block Public Access for all buckets and remove public bucket policies")
                        .severity("HIGH")
                        .resourceId(String.join(",", publicBuckets))
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in S3 public access check", e);
            return createErrorResult("CIS-2.1.5", "S3 Buckets Not Publicly Accessible", e);
        }
    }

    /**
     * CIS Check 2: Ensure all S3 buckets have encryption enabled
     */
    private CISCheckResult checkS3BucketsEncrypted() {
        log.info("Running CIS Check: S3 Buckets Encrypted");
        try {
            List<S3BucketInfo> buckets = s3DiscoveryService.discoverBuckets();
            List<String> unencryptedBuckets = new ArrayList<>();

            for (S3BucketInfo bucket : buckets) {
                if (!bucket.isEncryptionEnabled()) {
                    unencryptedBuckets.add(bucket.getBucketName());
                }
            }

            if (unencryptedBuckets.isEmpty()) {
                return CISCheckResult.builder()
                        .checkId("CIS-2.1.1")
                        .checkName("S3 Bucket Encryption Enabled")
                        .description("Ensure S3 bucket encryption is enabled")
                        .status("PASS")
                        .evidence(String.format("All %d S3 buckets have encryption enabled", buckets.size()))
                        .recommendation("N/A")
                        .severity("MEDIUM")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            } else {
                return CISCheckResult.builder()
                        .checkId("CIS-2.1.1")
                        .checkName("S3 Bucket Encryption Enabled")
                        .description("Ensure S3 bucket encryption is enabled")
                        .status("FAIL")
                        .evidence(String.format("Found %d unencrypted buckets: %s",
                                unencryptedBuckets.size(), String.join(", ", unencryptedBuckets)))
                        .recommendation("Enable default encryption (AES-256 or AWS-KMS) for all S3 buckets")
                        .severity("MEDIUM")
                        .resourceId(String.join(",", unencryptedBuckets))
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in S3 encryption check", e);
            return createErrorResult("CIS-2.1.1", "S3 Bucket Encryption Enabled", e);
        }
    }

    /**
     * CIS Check 3: Ensure IAM root account has MFA enabled
     */
    private CISCheckResult checkIAMRootMFAEnabled() {
        log.info("Running CIS Check: IAM Root MFA Enabled");
        try {
            GetAccountSummaryRequest request = GetAccountSummaryRequest.builder().build();
            GetAccountSummaryResponse response = iamClient.getAccountSummary(request);

            Integer mfaDevices = response.summaryMap().get("AccountMFAEnabled");
            boolean mfaEnabled = mfaDevices != null && mfaDevices > 0;

            if (mfaEnabled) {
                return CISCheckResult.builder()
                        .checkId("CIS-1.5")
                        .checkName("IAM Root Account MFA Enabled")
                        .description("Ensure MFA is enabled for the root account")
                        .status("PASS")
                        .evidence("MFA is enabled for the root account")
                        .recommendation("N/A")
                        .severity("HIGH")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            } else {
                return CISCheckResult.builder()
                        .checkId("CIS-1.5")
                        .checkName("IAM Root Account MFA Enabled")
                        .description("Ensure MFA is enabled for the root account")
                        .status("FAIL")
                        .evidence("MFA is NOT enabled for the root account")
                        .recommendation(
                                "Enable MFA for the root account immediately. Use virtual MFA or hardware MFA device.")
                        .severity("HIGH")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in IAM root MFA check", e);
            return createErrorResult("CIS-1.5", "IAM Root Account MFA Enabled", e);
        }
    }

    /**
     * CIS Check 4: Ensure CloudTrail is enabled
     */
    private CISCheckResult checkCloudTrailEnabled() {
        log.info("Running CIS Check: CloudTrail Enabled");
        try {
            DescribeTrailsRequest request = DescribeTrailsRequest.builder().build();
            DescribeTrailsResponse response = cloudTrailClient.describeTrails(request);

            List<Trail> trails = response.trailList();
            boolean hasEnabledTrail = !trails.isEmpty();

            if (hasEnabledTrail) {
                return CISCheckResult.builder()
                        .checkId("CIS-3.1")
                        .checkName("CloudTrail Enabled")
                        .description("Ensure CloudTrail is enabled in all regions")
                        .status("PASS")
                        .evidence(String.format("Found %d CloudTrail trail(s) configured", trails.size()))
                        .recommendation("N/A")
                        .severity("HIGH")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            } else {
                return CISCheckResult.builder()
                        .checkId("CIS-3.1")
                        .checkName("CloudTrail Enabled")
                        .description("Ensure CloudTrail is enabled in all regions")
                        .status("FAIL")
                        .evidence("No CloudTrail trails found")
                        .recommendation("Enable CloudTrail in all regions for audit logging and compliance")
                        .severity("HIGH")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in CloudTrail check", e);
            return createErrorResult("CIS-3.1", "CloudTrail Enabled", e);
        }
    }

    /**
     * CIS Check 5: Ensure security groups don't allow unrestricted access
     * (0.0.0.0/0) for SSH (22) or RDP (3389)
     */
    private CISCheckResult checkSecurityGroupsNotOpenToWorld() {
        log.info("Running CIS Check: Security Groups Not Open to World");
        try {
            List<SecurityGroup> securityGroups = ec2DiscoveryService.getSecurityGroups();
            List<String> offendingSGs = new ArrayList<>();

            for (SecurityGroup sg : securityGroups) {
                for (IpPermission permission : sg.ipPermissions()) {
                    Integer fromPort = permission.fromPort();
                    Integer toPort = permission.toPort();

                    // Check for SSH (22) or RDP (3389)
                    if ((fromPort != null && toPort != null) &&
                            ((fromPort <= 22 && toPort >= 22) || (fromPort <= 3389 && toPort >= 3389))) {

                        for (IpRange ipRange : permission.ipRanges()) {
                            if ("0.0.0.0/0".equals(ipRange.cidrIp())) {
                                String port = (fromPort <= 22 && toPort >= 22) ? "22 (SSH)" : "3389 (RDP)";
                                offendingSGs.add(String.format("%s (Port %s)", sg.groupId(), port));
                                break;
                            }
                        }
                    }
                }
            }

            if (offendingSGs.isEmpty()) {
                return CISCheckResult.builder()
                        .checkId("CIS-5.2")
                        .checkName("Security Groups Restricted Access")
                        .description("Ensure no security groups allow unrestricted access on SSH or RDP")
                        .status("PASS")
                        .evidence(
                                String.format("All %d security groups are properly restricted", securityGroups.size()))
                        .recommendation("N/A")
                        .severity("HIGH")
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            } else {
                return CISCheckResult.builder()
                        .checkId("CIS-5.2")
                        .checkName("Security Groups Restricted Access")
                        .description("Ensure no security groups allow unrestricted access on SSH or RDP")
                        .status("FAIL")
                        .evidence(String.format("Found %d security group(s) with unrestricted access: %s",
                                offendingSGs.size(), String.join(", ", offendingSGs)))
                        .recommendation(
                                "Restrict security group rules to specific IP addresses. Never use 0.0.0.0/0 for SSH or RDP")
                        .severity("HIGH")
                        .resourceId(String.join(",", offendingSGs))
                        .scanTimestamp(System.currentTimeMillis())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error in security group check", e);
            return createErrorResult("CIS-5.2", "Security Groups Restricted Access", e);
        }
    }

    private CISCheckResult createErrorResult(String checkId, String checkName, Exception e) {
        return CISCheckResult.builder()
                .checkId(checkId)
                .checkName(checkName)
                .description("Check encountered an error")
                .status("WARNING")
                .evidence("Error: " + e.getMessage())
                .recommendation("Review AWS permissions and configuration")
                .severity("MEDIUM")
                .scanTimestamp(System.currentTimeMillis())
                .build();
    }
}
