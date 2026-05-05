package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class SignalResponse {
    private Long id;
    private Long tropelId;
    private String tropelName;
    private Long guardianId;
    private String guardianName;
    private String senderTag;
    private String rawContent;
    private String signalType;
    private String severity;
    private String assignedUnit;
    private String recommendedAction;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
