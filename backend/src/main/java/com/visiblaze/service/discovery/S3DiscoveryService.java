package com.visiblaze.service.discovery;

import com.visiblaze.model.S3BucketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3DiscoveryService {

    private final S3Client s3Client;

    public List<S3BucketInfo> discoverBuckets() {
        log.info("Starting S3 bucket discovery...");
        List<S3BucketInfo> buckets = new ArrayList<>();

        try {
            ListBucketsResponse response = s3Client.listBuckets();

            for (Bucket bucket : response.buckets()) {
                try {
                    S3BucketInfo bucketInfo = buildBucketInfo(bucket);
                    buckets.add(bucketInfo);
                    log.debug("Discovered bucket: {}", bucketInfo.getBucketName());
                } catch (Exception e) {
                    log.warn("Error processing bucket {}: {}", bucket.name(), e.getMessage());
                    // Continue processing other buckets
                }
            }

            log.info("Discovered {} S3 buckets", buckets.size());
        } catch (Exception e) {
            log.error("Error discovering S3 buckets", e);
            throw new RuntimeException("Failed to discover S3 buckets: " + e.getMessage(), e);
        }

        return buckets;
    }

    private S3BucketInfo buildBucketInfo(Bucket bucket) {
        String bucketName = bucket.name();
        String region = getBucketRegion(bucketName);
        boolean encryptionEnabled = isBucketEncrypted(bucketName);
        String encryptionType = getEncryptionType(bucketName);
        String accessPolicy = getBucketAccessPolicy(bucketName);
        boolean blockPublicAccess = isPublicAccessBlocked(bucketName);
        boolean versioningEnabled = isVersioningEnabled(bucketName);

        return S3BucketInfo.builder()
                .bucketName(bucketName)
                .region(region)
                .encryptionEnabled(encryptionEnabled)
                .encryptionType(encryptionType)
                .accessPolicy(accessPolicy)
                .blockPublicAccess(blockPublicAccess)
                .versioningEnabled(versioningEnabled)
                .creationDate(bucket.creationDate() != null ? bucket.creationDate().toString() : "N/A")
                .scanTimestamp(System.currentTimeMillis())
                .build();
    }

    private String getBucketRegion(String bucketName) {
        try {
            GetBucketLocationRequest request = GetBucketLocationRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetBucketLocationResponse response = s3Client.getBucketLocation(request);
            String region = response.locationConstraintAsString();
            return (region == null || region.isEmpty()) ? "us-east-1" : region;
        } catch (Exception e) {
            log.warn("Could not get region for bucket {}: {}", bucketName, e.getMessage());
            return "unknown";
        }
    }

    private boolean isBucketEncrypted(String bucketName) {
        try {
            GetBucketEncryptionRequest request = GetBucketEncryptionRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.getBucketEncryption(request);
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false; // No encryption configured
            }
            log.warn("Error checking encryption for bucket {}: {}", bucketName, e.getMessage());
            return false;
        }
    }

    private String getEncryptionType(String bucketName) {
        try {
            GetBucketEncryptionRequest request = GetBucketEncryptionRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetBucketEncryptionResponse response = s3Client.getBucketEncryption(request);
            if (response.serverSideEncryptionConfiguration().rules().isEmpty()) {
                return "NONE";
            }
            return response.serverSideEncryptionConfiguration().rules().get(0)
                    .applyServerSideEncryptionByDefault().sseAlgorithmAsString();
        } catch (Exception e) {
            return "NONE";
        }
    }

    private String getBucketAccessPolicy(String bucketName) {
        try {
            GetBucketPolicyStatusRequest request = GetBucketPolicyStatusRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetBucketPolicyStatusResponse response = s3Client.getBucketPolicyStatus(request);
            return response.policyStatus().isPublic() ? "PUBLIC" : "PRIVATE";
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return "PRIVATE"; // No policy means private
            }
            log.warn("Error checking policy for bucket {}: {}", bucketName, e.getMessage());
            return "UNKNOWN";
        }
    }

    private boolean isPublicAccessBlocked(String bucketName) {
        try {
            GetPublicAccessBlockRequest request = GetPublicAccessBlockRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetPublicAccessBlockResponse response = s3Client.getPublicAccessBlock(request);
            PublicAccessBlockConfiguration config = response.publicAccessBlockConfiguration();
            return config.blockPublicAcls() && config.blockPublicPolicy() &&
                    config.ignorePublicAcls() && config.restrictPublicBuckets();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false; // No public access block configured
            }
            log.warn("Error checking public access block for bucket {}: {}", bucketName, e.getMessage());
            return false;
        }
    }

    private boolean isVersioningEnabled(String bucketName) {
        try {
            GetBucketVersioningRequest request = GetBucketVersioningRequest.builder()
                    .bucket(bucketName)
                    .build();
            GetBucketVersioningResponse response = s3Client.getBucketVersioning(request);
            return response.status() == BucketVersioningStatus.ENABLED;
        } catch (Exception e) {
            log.warn("Error checking versioning for bucket {}: {}", bucketName, e.getMessage());
            return false;
        }
    }
}
