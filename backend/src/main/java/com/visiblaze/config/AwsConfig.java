package com.visiblaze.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (accessKey != null && !accessKey.isBlank() && !accessKey.contains("YOUR_ACCESS_KEY")) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public Ec2Client ec2Client(AwsCredentialsProvider credentialsProvider) {
        return Ec2Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public IamClient iamClient(AwsCredentialsProvider credentialsProvider) {
        return IamClient.builder()
                .region(Region.AWS_GLOBAL)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public CloudTrailClient cloudTrailClient(AwsCredentialsProvider credentialsProvider) {
        return CloudTrailClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient(AwsCredentialsProvider credentialsProvider) {
        return DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
