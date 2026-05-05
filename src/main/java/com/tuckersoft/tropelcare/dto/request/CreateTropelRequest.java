package com.tuckersoft.tropelcare.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTropelRequest {
    @NotBlank
    @Size(min = 2, max = 40)
    private String name;

    @NotBlank
    private String species;

    @NotNull
    private Long sectorId;

    @NotNull
    private Long guardianId;
}
