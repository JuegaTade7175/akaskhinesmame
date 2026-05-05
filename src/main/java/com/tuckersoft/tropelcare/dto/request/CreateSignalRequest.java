package com.tuckersoft.tropelcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSignalRequest {
    @NotNull
    private Long tropelId;

    @NotNull
    private Long guardianId;

    @NotBlank
    private String senderTag;

    @NotBlank
    @Size(min = 10)
    private String rawContent;
}
