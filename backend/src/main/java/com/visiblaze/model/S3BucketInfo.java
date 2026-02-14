package com.visiblaze.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3BucketInfo {
    private String bucketName;
    private String region;
    private boolean encryptionEnabled;
    private String encryptionType;
    private String accessPolicy; // PUBLIC or PRIVATE
    private boolean blockPublicAccess;
    private boolean versioningEnabled;
    private String creationDate;
    private Long scanTimestamp;
}
