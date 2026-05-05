package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CareResponseDto {
    private Long id;
    private Long signalId;
    private String responseCode;
    private String description;
    private Instant createdAt;
}
