package com.visiblaze.service;

import com.visiblaze.model.*;
import com.visiblaze.service.benchmark.CISBenchmarkService;
import com.visiblaze.service.discovery.EC2DiscoveryService;
import com.visiblaze.service.discovery.S3DiscoveryService;
import com.visiblaze.service.storage.DynamoDbStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService {

    private final EC2DiscoveryService ec2DiscoveryService;
    private final S3DiscoveryService s3DiscoveryService;
    private final CISBenchmarkService cisBenchmarkService;
    private final DynamoDbStorageService storageService;

    public ScanResponse executeScan() {
        String scanId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();

        log.info("Starting scan with ID: {}", scanId);

        ScanResponse.ScanResponseBuilder responseBuilder = ScanResponse.builder()
                .scanId(scanId)
                .startTime(startTime)
                .status("IN_PROGRESS");

        try {
            // Ensure DynamoDB tables exist
            storageService.createTablesIfNotExist();

            // Discover EC2 instances
            List<EC2InstanceInfo> ec2Instances = new ArrayList<>();
            try {
                ec2Instances = ec2DiscoveryService.discoverInstances();
                storageService.storeEC2Instances(ec2Instances);
            } catch (Exception e) {
                log.error("Error discovering EC2 instances", e);
                errors.add("EC2 Discovery: " + e.getMessage());
            }

            // Discover S3 buckets
            List<S3BucketInfo> s3Buckets = new ArrayList<>();
            try {
                s3Buckets = s3DiscoveryService.discoverBuckets();
                storageService.storeS3Buckets(s3Buckets);
            } catch (Exception e) {
                log.error("Error discovering S3 buckets", e);
                errors.add("S3 Discovery: " + e.getMessage());
            }

            // Run CIS benchmark checks
            List<CISCheckResult> cisResults = new ArrayList<>();
            try {
                cisResults = cisBenchmarkService.runAllChecks();
                storageService.storeCISResults(cisResults);
            } catch (Exception e) {
                log.error("Error running CIS checks", e);
                errors.add("CIS Checks: " + e.getMessage());
            }

            // Calculate metrics
            int checksPassed = (int) cisResults.stream()
                    .filter(r -> "PASS".equals(r.getStatus()))
                    .count();
            int checksFailed = (int) cisResults.stream()
                    .filter(r -> "FAIL".equals(r.getStatus()))
                    .count();

            long endTime = System.currentTimeMillis();
            String status = errors.isEmpty() ? "COMPLETED" : "COMPLETED_WITH_ERRORS";

            ScanResponse response = responseBuilder
                    .endTime(endTime)
                    .status(status)
                    .ec2InstancesFound(ec2Instances.size())
                    .s3BucketsFound(s3Buckets.size())
                    .checksPerformed(cisResults.size())
                    .checksPassed(checksPassed)
                    .checksFailed(checksFailed)
                    .errors(errors)
                    .build();

            log.info("Scan {} completed. EC2: {}, S3: {}, Checks: {}/{} passed",
                    scanId, ec2Instances.size(), s3Buckets.size(), checksPassed, cisResults.size());

            return response;

        } catch (Exception e) {
            log.error("Critical error during scan execution", e);
            return responseBuilder
                    .endTime(System.currentTimeMillis())
                    .status("FAILED")
                    .errors(List.of("Critical error: " + e.getMessage()))
                    .build();
        }
    }
}
