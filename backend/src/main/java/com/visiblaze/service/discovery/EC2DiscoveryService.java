package com.visiblaze.service.discovery;

import com.visiblaze.model.EC2InstanceInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EC2DiscoveryService {

    private final Ec2Client ec2Client;

    public List<EC2InstanceInfo> discoverInstances() {
        log.info("Starting EC2 instance discovery...");
        List<EC2InstanceInfo> instances = new ArrayList<>();

        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            DescribeInstancesResponse response = ec2Client.describeInstances(request);

            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    EC2InstanceInfo instanceInfo = buildInstanceInfo(instance);
                    instances.add(instanceInfo);
                    log.debug("Discovered instance: {}", instanceInfo.getInstanceId());
                }
            }

            log.info("Discovered {} EC2 instances", instances.size());
        } catch (Exception e) {
            log.error("Error discovering EC2 instances", e);
            throw new RuntimeException("Failed to discover EC2 instances: " + e.getMessage(), e);
        }

        return instances;
    }

    private EC2InstanceInfo buildInstanceInfo(Instance instance) {
        List<String> securityGroups = instance.securityGroups().stream()
                .map(GroupIdentifier::groupId)
                .collect(Collectors.toList());

        return EC2InstanceInfo.builder()
                .instanceId(instance.instanceId())
                .instanceType(instance.instanceType().toString())
                .region(getRegionFromAvailabilityZone(instance.placement().availabilityZone()))
                .publicIp(instance.publicIpAddress() != null ? instance.publicIpAddress() : "N/A")
                .privateIp(instance.privateIpAddress() != null ? instance.privateIpAddress() : "N/A")
                .state(instance.state().name().toString())
                .securityGroups(securityGroups)
                .availabilityZone(instance.placement().availabilityZone())
                .launchTime(instance.launchTime() != null ? instance.launchTime().toString() : "N/A")
                .scanTimestamp(System.currentTimeMillis())
                .build();
    }

    private String getRegionFromAvailabilityZone(String availabilityZone) {
        if (availabilityZone == null || availabilityZone.isEmpty()) {
            return "unknown";
        }
        // Remove the last character (zone letter) to get region
        return availabilityZone.substring(0, availabilityZone.length() - 1);
    }

    public List<SecurityGroup> getSecurityGroups() {
        log.info("Retrieving all security groups...");
        try {
            DescribeSecurityGroupsRequest request = DescribeSecurityGroupsRequest.builder().build();
            DescribeSecurityGroupsResponse response = ec2Client.describeSecurityGroups(request);
            log.info("Retrieved {} security groups", response.securityGroups().size());
            return response.securityGroups();
        } catch (Exception e) {
            log.error("Error retrieving security groups", e);
            throw new RuntimeException("Failed to retrieve security groups: " + e.getMessage(), e);
        }
    }
}
