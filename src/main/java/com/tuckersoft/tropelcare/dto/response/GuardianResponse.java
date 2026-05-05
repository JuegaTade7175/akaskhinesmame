package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class GuardianResponse {
    private Long id;
    private String displayName;
    private String email;
    private String notificationEmail;
    private Instant createdAt;
}
