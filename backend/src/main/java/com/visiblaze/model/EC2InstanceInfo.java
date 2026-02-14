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
public class EC2InstanceInfo {
    private String instanceId;
    private String instanceType;
    private String region;
    private String publicIp;
    private String privateIp;
    private String state;
    private List<String> securityGroups;
    private String availabilityZone;
    private String launchTime;
    private Long scanTimestamp;
}
