package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateSectorRequest;
import com.tuckersoft.tropelcare.dto.response.SectorResponse;
import com.tuckersoft.tropelcare.entity.Sector;
import com.tuckersoft.tropelcare.exception.ConflictException;
import com.tuckersoft.tropelcare.repository.SectorRepository;
import com.tuckersoft.tropelcare.service.impl.SectorServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectorServiceTest {

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private SectorServiceImpl sectorService;

    @Test
    void create_newSector_returnsCreatedResponse() {
        CreateSectorRequest req = new CreateSectorRequest();
        req.setSectorCode("SECTOR-7");
        req.setClimate("RETRO_ARCADE");
        req.setCapacity(3);

        when(sectorRepository.existsBySectorCode("SECTOR-7")).thenReturn(false);

        Sector saved = new Sector();
        saved.setId(1L);
        saved.setSectorCode("SECTOR-7");
        saved.setClimate("RETRO_ARCADE");
        saved.setCapacity(3);
        saved.setCurrentLoad(0);
        saved.setStabilityLevel(100);
        saved.setCreatedAt(Instant.now());

        when(sectorRepository.save(any())).thenReturn(saved);

        SectorResponse response = sectorService.create(req);

        assertThat(response.getSectorCode()).isEqualTo("SECTOR-7");
        assertThat(response.getCurrentLoad()).isEqualTo(0);
        assertThat(response.getStabilityLevel()).isEqualTo(100);
    }

    @Test
    void create_duplicateSectorCode_throwsConflict() {
        CreateSectorRequest req = new CreateSectorRequest();
        req.setSectorCode("SECTOR-7");
        req.setClimate("NEON_CAVE");
        req.setCapacity(5);

        when(sectorRepository.existsBySectorCode("SECTOR-7")).thenReturn(true);

        assertThatThrownBy(() -> sectorService.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SECTOR-7");
    }
}
