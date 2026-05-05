package com.tuckersoft.tropelcare.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class DiaryResponse {
    private Long tropelId;
    private String tropelName;
    private List<DiaryNoteResponse> notes;

    @Getter
    @Builder
    public static class DiaryNoteResponse {
        private Long signalId;
        private String personalityNote;
        private Instant createdAt;
    }
}
