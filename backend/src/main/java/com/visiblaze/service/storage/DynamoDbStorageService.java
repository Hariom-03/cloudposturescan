package com.visiblaze.service.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiblaze.model.CISCheckResult;
import com.visiblaze.model.EC2InstanceInfo;
import com.visiblaze.model.S3BucketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDbStorageService {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${aws.dynamodb.table.ec2-instances}")
    private String ec2TableName;

    @Value("${aws.dynamodb.table.s3-buckets}")
    private String s3TableName;

    @Value("${aws.dynamodb.table.cis-results}")
    private String cisTableName;

    public void createTablesIfNotExist() {
        createEC2Table();
        createS3Table();
        createCISTable();
    }

    private void createEC2Table() {
        try {
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(ec2TableName)
                    .build();
            dynamoDbClient.describeTable(describeRequest);
            log.info("EC2 table already exists: {}", ec2TableName);
        } catch (ResourceNotFoundException e) {
            log.info("Creating EC2 table: {}", ec2TableName);
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(ec2TableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("instanceId")
                                    .keyType(KeyType.HASH)
                                    .build())
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("instanceId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(request);
            log.info("Created EC2 table: {}", ec2TableName);
        }
    }

    private void createS3Table() {
        try {
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(s3TableName)
                    .build();
            dynamoDbClient.describeTable(describeRequest);
            log.info("S3 table already exists: {}", s3TableName);
        } catch (ResourceNotFoundException e) {
            log.info("Creating S3 table: {}", s3TableName);
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(s3TableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("bucketName")
                                    .keyType(KeyType.HASH)
                                    .build())
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("bucketName")
                                    .attributeType(ScalarAttributeType.S)
                                    .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(request);
            log.info("Created S3 table: {}", s3TableName);
        }
    }

    private void createCISTable() {
        try {
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(cisTableName)
                    .build();
            dynamoDbClient.describeTable(describeRequest);
            log.info("CIS table already exists: {}", cisTableName);
        } catch (ResourceNotFoundException e) {
            log.info("Creating CIS table: {}", cisTableName);
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(cisTableName)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("checkId")
                                    .keyType(KeyType.HASH)
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName("scanTimestamp")
                                    .keyType(KeyType.RANGE)
                                    .build())
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("checkId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("scanTimestamp")
                                    .attributeType(ScalarAttributeType.N)
                                    .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(request);
            log.info("Created CIS table: {}", cisTableName);
        }
    }

    public void storeEC2Instances(List<EC2InstanceInfo> instances) {
        log.info("Storing {} EC2 instances to DynamoDB", instances.size());
        for (EC2InstanceInfo instance : instances) {
            try {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("instanceId", AttributeValue.builder().s(instance.getInstanceId()).build());
                item.put("instanceType", AttributeValue.builder().s(instance.getInstanceType()).build());
                item.put("region", AttributeValue.builder().s(instance.getRegion()).build());
                item.put("publicIp", AttributeValue.builder().s(instance.getPublicIp()).build());
                item.put("privateIp", AttributeValue.builder().s(instance.getPrivateIp()).build());
                item.put("state", AttributeValue.builder().s(instance.getState()).build());
                item.put("securityGroups",
                        AttributeValue.builder().s(String.join(",", instance.getSecurityGroups())).build());
                item.put("availabilityZone", AttributeValue.builder().s(instance.getAvailabilityZone()).build());
                item.put("launchTime", AttributeValue.builder().s(instance.getLaunchTime()).build());
                item.put("scanTimestamp",
                        AttributeValue.builder().n(String.valueOf(instance.getScanTimestamp())).build());

                PutItemRequest request = PutItemRequest.builder()
                        .tableName(ec2TableName)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(request);
            } catch (Exception e) {
                log.error("Error storing EC2 instance {}: {}", instance.getInstanceId(), e.getMessage());
            }
        }
        log.info("Successfully stored EC2 instances");
    }

    public void storeS3Buckets(List<S3BucketInfo> buckets) {
        log.info("Storing {} S3 buckets to DynamoDB", buckets.size());
        for (S3BucketInfo bucket : buckets) {
            try {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("bucketName", AttributeValue.builder().s(bucket.getBucketName()).build());
                item.put("region", AttributeValue.builder().s(bucket.getRegion()).build());
                item.put("encryptionEnabled", AttributeValue.builder().bool(bucket.isEncryptionEnabled()).build());
                item.put("encryptionType", AttributeValue.builder().s(bucket.getEncryptionType()).build());
                item.put("accessPolicy", AttributeValue.builder().s(bucket.getAccessPolicy()).build());
                item.put("blockPublicAccess", AttributeValue.builder().bool(bucket.isBlockPublicAccess()).build());
                item.put("versioningEnabled", AttributeValue.builder().bool(bucket.isVersioningEnabled()).build());
                item.put("creationDate", AttributeValue.builder().s(bucket.getCreationDate()).build());
                item.put("scanTimestamp",
                        AttributeValue.builder().n(String.valueOf(bucket.getScanTimestamp())).build());

                PutItemRequest request = PutItemRequest.builder()
                        .tableName(s3TableName)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(request);
            } catch (Exception e) {
                log.error("Error storing S3 bucket {}: {}", bucket.getBucketName(), e.getMessage());
            }
        }
        log.info("Successfully stored S3 buckets");
    }

    public void storeCISResults(List<CISCheckResult> results) {
        log.info("Storing {} CIS check results to DynamoDB", results.size());
        for (CISCheckResult result : results) {
            try {
                Map<String, AttributeValue> item = new HashMap<>();
                item.put("checkId", AttributeValue.builder().s(result.getCheckId()).build());
                item.put("scanTimestamp",
                        AttributeValue.builder().n(String.valueOf(result.getScanTimestamp())).build());
                item.put("checkName", AttributeValue.builder().s(result.getCheckName()).build());
                item.put("description", AttributeValue.builder().s(result.getDescription()).build());
                item.put("status", AttributeValue.builder().s(result.getStatus()).build());
                item.put("evidence", AttributeValue.builder().s(result.getEvidence()).build());
                item.put("recommendation", AttributeValue.builder().s(result.getRecommendation()).build());
                item.put("severity", AttributeValue.builder().s(result.getSeverity()).build());
                if (result.getResourceId() != null) {
                    item.put("resourceId", AttributeValue.builder().s(result.getResourceId()).build());
                }

                PutItemRequest request = PutItemRequest.builder()
                        .tableName(cisTableName)
                        .item(item)
                        .build();

                dynamoDbClient.putItem(request);
            } catch (Exception e) {
                log.error("Error storing CIS result {}: {}", result.getCheckId(), e.getMessage());
            }
        }
        log.info("Successfully stored CIS results");
    }

    public List<EC2InstanceInfo> getEC2Instances() {
        log.info("Retrieving EC2 instances from DynamoDB");
        List<EC2InstanceInfo> instances = new ArrayList<>();

        try {
            ScanRequest request = ScanRequest.builder()
                    .tableName(ec2TableName)
                    .build();

            ScanResponse response = dynamoDbClient.scan(request);

            for (Map<String, AttributeValue> item : response.items()) {
                EC2InstanceInfo instance = EC2InstanceInfo.builder()
                        .instanceId(item.get("instanceId").s())
                        .instanceType(item.get("instanceType").s())
                        .region(item.get("region").s())
                        .publicIp(item.get("publicIp").s())
                        .privateIp(item.get("privateIp").s())
                        .state(item.get("state").s())
                        .securityGroups(Arrays.asList(item.get("securityGroups").s().split(",")))
                        .availabilityZone(item.get("availabilityZone").s())
                        .launchTime(item.get("launchTime").s())
                        .scanTimestamp(Long.parseLong(item.get("scanTimestamp").n()))
                        .build();
                instances.add(instance);
            }
        } catch (ResourceNotFoundException e) {
            log.info("EC2 table does not exist yet: {}", ec2TableName);
        } catch (Exception e) {
            log.error("Error retrieving EC2 instances from DynamoDB", e);
        }

        return instances;
    }

    public List<S3BucketInfo> getS3Buckets() {
        log.info("Retrieving S3 buckets from DynamoDB");
        List<S3BucketInfo> buckets = new ArrayList<>();

        try {
            ScanRequest request = ScanRequest.builder()
                    .tableName(s3TableName)
                    .build();

            ScanResponse response = dynamoDbClient.scan(request);

            for (Map<String, AttributeValue> item : response.items()) {
                S3BucketInfo bucket = S3BucketInfo.builder()
                        .bucketName(item.get("bucketName").s())
                        .region(item.get("region").s())
                        .encryptionEnabled(item.get("encryptionEnabled").bool())
                        .encryptionType(item.get("encryptionType").s())
                        .accessPolicy(item.get("accessPolicy").s())
                        .blockPublicAccess(item.get("blockPublicAccess").bool())
                        .versioningEnabled(item.get("versioningEnabled").bool())
                        .creationDate(item.get("creationDate").s())
                        .scanTimestamp(Long.parseLong(item.get("scanTimestamp").n()))
                        .build();
                buckets.add(bucket);
            }
        } catch (ResourceNotFoundException e) {
            log.info("S3 table does not exist yet: {}", s3TableName);
        } catch (Exception e) {
            log.error("Error retrieving S3 buckets from DynamoDB", e);
        }

        return buckets;
    }

    public List<CISCheckResult> getCISResults() {
        log.info("Retrieving CIS results from DynamoDB");
        List<CISCheckResult> results = new ArrayList<>();

        try {
            ScanRequest request = ScanRequest.builder()
                    .tableName(cisTableName)
                    .build();

            ScanResponse response = dynamoDbClient.scan(request);

            for (Map<String, AttributeValue> item : response.items()) {
                CISCheckResult result = CISCheckResult.builder()
                        .checkId(item.get("checkId").s())
                        .checkName(item.get("checkName").s())
                        .description(item.get("description").s())
                        .status(item.get("status").s())
                        .evidence(item.get("evidence").s())
                        .recommendation(item.get("recommendation").s())
                        .severity(item.get("severity").s())
                        .scanTimestamp(Long.parseLong(item.get("scanTimestamp").n()))
                        .build();

                if (item.containsKey("resourceId")) {
                    result.setResourceId(item.get("resourceId").s());
                }

                results.add(result);
            }

            // Sort by timestamp descending to get latest results first
            results.sort((a, b) -> Long.compare(b.getScanTimestamp(), a.getScanTimestamp()));
        } catch (ResourceNotFoundException e) {
            log.info("CIS results table does not exist yet: {}", cisTableName);
        } catch (Exception e) {
            log.error("Error retrieving CIS results from DynamoDB", e);
        }

        return results;
    }
}
