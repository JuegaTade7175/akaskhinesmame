package com.tuckersoft.tropelcare.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSectorRequest {
    @NotBlank
    private String sectorCode;
    @NotBlank
    private String climate;
    private Integer capacity;
}
