package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateSignalRequest;
import com.tuckersoft.tropelcare.dto.response.SignalResponse;
import com.tuckersoft.tropelcare.entity.Guardian;
import com.tuckersoft.tropelcare.entity.Sector;
import com.tuckersoft.tropelcare.entity.Tropel;
import com.tuckersoft.tropelcare.entity.TropelSignal;
import com.tuckersoft.tropelcare.exception.BadRequestException;
import com.tuckersoft.tropelcare.repository.*;
import com.tuckersoft.tropelcare.service.impl.SignalServiceImpl;
import com.tuckersoft.tropelcare.util.GithubModelsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock private TropelRepository tropelRepository;
    @Mock private GuardianRepository guardianRepository;
    @Mock private TropelSignalRepository signalRepository;
    @Mock private CareResponseRepository careResponseRepository;
    @Mock private SectorRepository sectorRepository;
    @Mock private GithubModelsClient githubModelsClient;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SignalServiceImpl signalService;

    private Guardian guardian;
    private Sector sector;
    private Tropel tropel;

    @BeforeEach
    void setUp() {
        guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("cam@gmail.com");
        guardian.setCreatedAt(Instant.now());

        sector = new Sector();
        sector.setId(1L);
        sector.setSectorCode("SECTOR-7");
        sector.setCapacity(3);
        sector.setCurrentLoad(1);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());

        tropel = new Tropel();
        tropel.setId(1L);
        tropel.setName("BipBop");
        tropel.setSpecies("GLITCHY");
        tropel.setVitalState("ESTABLE");
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(Instant.now());
        tropel.setUpdatedAt(Instant.now());
    }

    @Test
    void create_wrongGuardianId_throwsBadRequest() {
        CreateSignalRequest req = new CreateSignalRequest();
        req.setTropelId(1L);
        req.setGuardianId(99L);
        req.setSenderTag("sensor-test");
        req.setRawContent("Señal de prueba para validar guardián incorrecto.");

        Guardian otroGuardian = new Guardian();
        otroGuardian.setId(99L);
        otroGuardian.setDisplayName("Otro");
        otroGuardian.setEmail("otro@mail.com");
        otroGuardian.setNotificationEmail("otro@mail.com");
        otroGuardian.setCreatedAt(Instant.now());

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(99L)).thenReturn(Optional.of(otroGuardian));

        assertThatThrownBy(() -> signalService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("guardianId no corresponde");
    }

    @Test
    void create_aiFailure_savesFallbackSignalWithErrorStatus() {
        CreateSignalRequest req = new CreateSignalRequest();
        req.setTropelId(1L);
        req.setGuardianId(1L);
        req.setSenderTag("sensor-norte-7");
        req.setRawContent("Señal corrupta que no puede ser clasificada por la IA.");

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.empty());

        TropelSignal fallbackSignal = new TropelSignal();
        fallbackSignal.setId(10L);
        fallbackSignal.setTropel(tropel);
        fallbackSignal.setGuardian(guardian);
        fallbackSignal.setSenderTag(req.getSenderTag());
        fallbackSignal.setRawContent(req.getRawContent());
        fallbackSignal.setSignalType("SENAL_CORRUPTA");
        fallbackSignal.setSeverity("LEVE");
        fallbackSignal.setAssignedUnit("Archivo de Senales");
        fallbackSignal.setRecommendedAction("Archivar la señal y revisar manualmente si se repite.");
        fallbackSignal.setStatus("ERROR");
        fallbackSignal.setCreatedAt(Instant.now());
        fallbackSignal.setUpdatedAt(Instant.now());

        when(signalRepository.save(any())).thenReturn(fallbackSignal);
        when(careResponseRepository.save(any())).thenReturn(null);

        SignalResponse response = signalService.create(req);

        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getSignalType()).isEqualTo("SENAL_CORRUPTA");
        verify(eventPublisher, never()).publishEvent(any());
    }
}
