package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class TropelResponse {
    private Long id;
    private String name;
    private String species;
    private String vitalState;
    private Integer energyLevel;
    private Integer chaosIndex;
    private Integer mutationStage;
    private Long sectorId;
    private String sectorCode;
    private Long guardianId;
    private String guardianName;
    private Instant createdAt;
    private Instant updatedAt;
}
