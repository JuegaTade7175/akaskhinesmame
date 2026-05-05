package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class NotificationLogDto {
    private Long id;
    private Long signalId;
    private String recipientEmail;
    private String subject;
    private String notifStatus;
    private String errorMessage;
    private Instant sentAt;
    private Instant createdAt;
}
