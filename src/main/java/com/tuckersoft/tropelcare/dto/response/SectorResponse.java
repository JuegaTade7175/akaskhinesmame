package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class SectorResponse {
    private Long id;
    private String sectorCode;
    private String climate;
    private Integer capacity;
    private Integer currentLoad;
    private Integer stabilityLevel;
    private Instant createdAt;
}
