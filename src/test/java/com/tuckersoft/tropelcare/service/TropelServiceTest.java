package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateTropelRequest;
import com.tuckersoft.tropelcare.dto.response.TropelResponse;
import com.tuckersoft.tropelcare.entity.Guardian;
import com.tuckersoft.tropelcare.entity.Sector;
import com.tuckersoft.tropelcare.entity.Tropel;
import com.tuckersoft.tropelcare.exception.BadRequestException;
import com.tuckersoft.tropelcare.exception.ConflictException;
import com.tuckersoft.tropelcare.repository.GuardianRepository;
import com.tuckersoft.tropelcare.repository.SectorRepository;
import com.tuckersoft.tropelcare.repository.TropelRepository;
import com.tuckersoft.tropelcare.service.impl.TropelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TropelServiceTest {

    @Mock private TropelRepository tropelRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private GuardianRepository guardianRepository;

    @InjectMocks
    private TropelServiceImpl tropelService;

    private Sector sector;
    private Guardian guardian;

    @BeforeEach
    void setUp() {
        sector = new Sector();
        sector.setId(1L);
        sector.setSectorCode("SECTOR-7");
        sector.setClimate("RETRO_ARCADE");
        sector.setCapacity(3);
        sector.setCurrentLoad(0);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("cam@gmail.com");
        guardian.setCreatedAt(Instant.now());
    }

    @Test
    void create_validTropel_returnsCreatedWithInitialStats() {
        CreateTropelRequest req = new CreateTropelRequest();
        req.setName("BipBop");
        req.setSpecies("GLITCHY");
        req.setSectorId(1L);
        req.setGuardianId(1L);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(tropelRepository.existsByName("BipBop")).thenReturn(false);

        Tropel saved = new Tropel();
        saved.setId(1L);
        saved.setName("BipBop");
        saved.setSpecies("GLITCHY");
        saved.setVitalState("ESTABLE");
        saved.setEnergyLevel(80);
        saved.setChaosIndex(10);
        saved.setMutationStage(0);
        saved.setSector(sector);
        saved.setGuardian(guardian);
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        when(tropelRepository.save(any())).thenReturn(saved);
        when(sectorRepository.save(any())).thenReturn(sector);

        TropelResponse response = tropelService.create(req);

        assertThat(response.getName()).isEqualTo("BipBop");
        assertThat(response.getVitalState()).isEqualTo("ESTABLE");
        assertThat(response.getEnergyLevel()).isEqualTo(80);
        assertThat(response.getChaosIndex()).isEqualTo(10);
        assertThat(response.getMutationStage()).isEqualTo(0);
    }

    @Test
    void create_sectorFull_throwsBadRequest() {
        sector.setCurrentLoad(3); // igual a capacity
        CreateTropelRequest req = new CreateTropelRequest();
        req.setName("ZapZap");
        req.setSpecies("CHISPA");
        req.setSectorId(1L);
        req.setGuardianId(1L);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));

        assertThatThrownBy(() -> tropelService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("lleno");
    }

    @Test
    void create_duplicateName_throwsConflict() {
        CreateTropelRequest req = new CreateTropelRequest();
        req.setName("BipBop");
        req.setSpecies("GLITCHY");
        req.setSectorId(1L);
        req.setGuardianId(1L);

        when(sectorRepository.findById(1L)).thenReturn(Optional.of(sector));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(tropelRepository.existsByName("BipBop")).thenReturn(true);

        assertThatThrownBy(() -> tropelService.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("BipBop");
    }
}
