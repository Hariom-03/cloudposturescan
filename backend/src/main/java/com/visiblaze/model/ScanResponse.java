package com.visiblaze.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponse {
    private String scanId;
    private String status;
    private Long startTime;
    private Long endTime;
    private int ec2InstancesFound;
    private int s3BucketsFound;
    private int checksPerformed;
    private int checksPassed;
    private int checksFailed;
    private List<String> errors;
}
