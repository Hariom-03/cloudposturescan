package com.visiblaze.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CISCheckResult {
    private String checkId;
    private String checkName;
    private String description;
    private String status; // PASS, FAIL, WARNING
    private String evidence;
    private String recommendation;
    private String severity; // HIGH, MEDIUM, LOW
    private Long scanTimestamp;
    private String resourceId; // Optional: specific resource that failed
}
