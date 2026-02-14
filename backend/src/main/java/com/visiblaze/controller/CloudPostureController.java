package com.visiblaze.controller;

import com.visiblaze.model.CISCheckResult;
import com.visiblaze.model.EC2InstanceInfo;
import com.visiblaze.model.S3BucketInfo;
import com.visiblaze.model.ScanResponse;
import com.visiblaze.service.ScanService;
import com.visiblaze.service.storage.DynamoDbStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class CloudPostureController {

    private final ScanService scanService;
    private final DynamoDbStorageService storageService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Cloud Posture Scanner");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> triggerScan() {
        log.info("Received request to trigger new scan");
        try {
            ScanResponse response = scanService.executeScan();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error executing scan", e);
            ScanResponse errorResponse = ScanResponse.builder()
                    .status("FAILED")
                    .errors(List.of(e.getMessage()))
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/instances")
    public ResponseEntity<List<EC2InstanceInfo>> getInstances() {
        log.info("Received request to retrieve EC2 instances");
        try {
            List<EC2InstanceInfo> instances = storageService.getEC2Instances();
            return ResponseEntity.ok(instances);
        } catch (Exception e) {
            log.error("Error retrieving EC2 instances", e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/buckets")
    public ResponseEntity<List<S3BucketInfo>> getBuckets() {
        log.info("Received request to retrieve S3 buckets");
        try {
            List<S3BucketInfo> buckets = storageService.getS3Buckets();
            return ResponseEntity.ok(buckets);
        } catch (Exception e) {
            log.error("Error retrieving S3 buckets", e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/cis-results")
    public ResponseEntity<List<CISCheckResult>> getCISResults() {
        log.info("Received request to retrieve CIS check results");
        try {
            List<CISCheckResult> results = storageService.getCISResults();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error retrieving CIS results", e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        log.info("Received request for dashboard summary");
        try {
            List<EC2InstanceInfo> instances = storageService.getEC2Instances();
            List<S3BucketInfo> buckets = storageService.getS3Buckets();
            List<CISCheckResult> cisResults = storageService.getCISResults();

            long passedChecks = cisResults.stream().filter(r -> "PASS".equals(r.getStatus())).count();
            long failedChecks = cisResults.stream().filter(r -> "FAIL".equals(r.getStatus())).count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalEC2Instances", instances.size());
            summary.put("totalS3Buckets", buckets.size());
            summary.put("totalCISChecks", cisResults.size());
            summary.put("checksPassedCount", passedChecks);
            summary.put("checksFailedCount", failedChecks);
            summary.put("complianceRate",
                    cisResults.isEmpty() ? 0 : Math.round((passedChecks * 100.0) / cisResults.size()));

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error generating dashboard summary", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
