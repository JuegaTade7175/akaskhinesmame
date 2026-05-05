package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateSignalRequest;
import com.tuckersoft.tropelcare.dto.response.SignalResponse;
import com.tuckersoft.tropelcare.entity.*;
import com.tuckersoft.tropelcare.exception.BadRequestException;
import com.tuckersoft.tropelcare.repository.*;
import com.tuckersoft.tropelcare.service.impl.SignalServiceImpl;
import com.tuckersoft.tropelcare.util.GithubModelsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    void create_validAiResponse_savesSignalWithCorrectFieldsAndStatusRecibida() {
        CreateSignalRequest req = buildRequest(1L, 1L,
                "BipBop lleva 3 ciclos sin recibir nutrientes.");

        GithubModelsClient.ClassificationResult result =
                new GithubModelsClient.ClassificationResult(
                        "HAMBRE", "MODERADO", "Laboratorio de Nutricion",
                        "Enviar paquete de nutrientes.", null);

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.of(result));

        TropelSignal savedSignal = buildFakeSignal(
                "HAMBRE", "MODERADO", "Laboratorio de Nutricion",
                "Enviar paquete de nutrientes.", "RECIBIDA");
        when(signalRepository.save(any())).thenReturn(savedSignal);
        when(tropelRepository.save(any())).thenReturn(tropel);
        when(sectorRepository.save(any())).thenReturn(sector);
        when(careResponseRepository.save(any())).thenReturn(null);

        SignalResponse response = signalService.create(req);

        assertThat(response.getSignalType()).isEqualTo("HAMBRE");
        assertThat(response.getSeverity()).isEqualTo("MODERADO");
        assertThat(response.getAssignedUnit()).isEqualTo("Laboratorio de Nutricion");
        assertThat(response.getStatus()).isEqualTo("RECIBIDA");
        verify(signalRepository).save(any());
    }

    @Test
    void githubModelsClient_aiResponseWithExtraText_parsesJsonCorrectly() {
        CreateSignalRequest req = buildRequest(1L, 1L,
                "BipBop emite un brillo verde anómalo.");

        GithubModelsClient.ClassificationResult result =
                new GithubModelsClient.ClassificationResult(
                        "MUTACION", "GRAVE", "Division Genetica",
                        "Aislar al Tropel.", null);

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.of(result));

        TropelSignal savedSignal = buildFakeSignal(
                "MUTACION", "GRAVE", "Division Genetica", "Aislar al Tropel.", "RECIBIDA");
        when(signalRepository.save(any())).thenReturn(savedSignal);
        when(tropelRepository.save(any())).thenReturn(tropel);
        when(sectorRepository.save(any())).thenReturn(sector);
        when(careResponseRepository.save(any())).thenReturn(null);

        assertThatNoException().isThrownBy(() -> signalService.create(req));

        SignalResponse response = signalService.create(req);
        assertThat(response.getSignalType()).isEqualTo("MUTACION");
    }

    @Test
    void create_aiFailure_savesFallbackSignalWithErrorStatus() {
        CreateSignalRequest req = buildRequest(1L, 1L,
                "%%zrkk THRNG 01101 %%%");

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.empty());

        TropelSignal fallbackSignal = buildFakeSignal(
                "SENAL_CORRUPTA", "LEVE", "Archivo de Senales",
                "Archivar la señal y revisar manualmente si se repite.", "ERROR");
        when(signalRepository.save(any())).thenReturn(fallbackSignal);
        when(careResponseRepository.save(any())).thenReturn(null);

        SignalResponse response = signalService.create(req);

        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getSignalType()).isEqualTo("SENAL_CORRUPTA");
        assertThat(response.getSeverity()).isEqualTo("LEVE");
        assertThat(response.getAssignedUnit()).isEqualTo("Archivo de Senales");
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void create_criticalSeverity_updatesTropelStatsCorrectly() {
        CreateSignalRequest req = buildRequest(1L, 1L,
                "BipBop emite dos extremidades adicionales que no existían.");

        GithubModelsClient.ClassificationResult result =
                new GithubModelsClient.ClassificationResult(
                        "MUTACION", "CRITICO", "Division Genetica",
                        "Aislar y observar.", null);

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.of(result));

        ArgumentCaptor<Tropel> tropelCaptor = ArgumentCaptor.forClass(Tropel.class);
        when(tropelRepository.save(tropelCaptor.capture())).thenReturn(tropel);
        when(sectorRepository.save(any())).thenReturn(sector);

        TropelSignal savedSignal = buildFakeSignal(
                "MUTACION", "CRITICO", "Division Genetica", "Aislar y observar.", "RECIBIDA");
        when(signalRepository.save(any())).thenReturn(savedSignal);
        when(careResponseRepository.save(any())).thenReturn(null);

        signalService.create(req);

        Tropel saved = tropelCaptor.getValue();
        assertThat(saved.getChaosIndex()).isEqualTo(55);
        assertThat(saved.getMutationStage()).isEqualTo(1);
        assertThat(saved.getEnergyLevel()).isEqualTo(50);
    }

    @Test
    void create_publishEvent_exactlyOnceOnSuccess_neverOnFallback() {
        CreateSignalRequest reqOk = buildRequest(1L, 1L, "Señal válida de hambre.");
        GithubModelsClient.ClassificationResult result =
                new GithubModelsClient.ClassificationResult(
                        "HAMBRE", "LEVE", "Laboratorio de Nutricion",
                        "Suministrar nutrientes.", null);

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.of(result));
        when(tropelRepository.save(any())).thenReturn(tropel);
        when(sectorRepository.save(any())).thenReturn(sector);
        TropelSignal okSignal = buildFakeSignal(
                "HAMBRE", "LEVE", "Laboratorio de Nutricion", "Suministrar nutrientes.", "RECIBIDA");
        when(signalRepository.save(any())).thenReturn(okSignal);
        when(careResponseRepository.save(any())).thenReturn(null);

        signalService.create(reqOk);
        verify(eventPublisher, times(1)).publishEvent(any());

        reset(eventPublisher, githubModelsClient, signalRepository, careResponseRepository);
        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));
        when(githubModelsClient.classify(any())).thenReturn(Optional.empty());
        TropelSignal fallback = buildFakeSignal(
                "SENAL_CORRUPTA", "LEVE", "Archivo de Senales",
                "Archivar la señal y revisar manualmente si se repite.", "ERROR");
        when(signalRepository.save(any())).thenReturn(fallback);
        when(careResponseRepository.save(any())).thenReturn(null);

        CreateSignalRequest reqFail = buildRequest(1L, 1L, "%%señal corrupta%%");
        signalService.create(reqFail);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void create_wrongGuardianId_throwsBadRequest() {
        CreateSignalRequest req = buildRequest(1L, 99L,
                "Señal de prueba para validar guardián incorrecto.");

        Guardian otroGuardian = new Guardian();
        otroGuardian.setId(99L);
        otroGuardian.setDisplayName("Intruso");
        otroGuardian.setEmail("intruso@mail.com");
        otroGuardian.setNotificationEmail("intruso@mail.com");
        otroGuardian.setCreatedAt(Instant.now());

        when(tropelRepository.findById(1L)).thenReturn(Optional.of(tropel));
        when(guardianRepository.findById(99L)).thenReturn(Optional.of(otroGuardian));

        assertThatThrownBy(() -> signalService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("guardianId no corresponde");

        verify(eventPublisher, never()).publishEvent(any());
    }

    private CreateSignalRequest buildRequest(Long tropelId, Long guardianId, String rawContent) {
        CreateSignalRequest req = new CreateSignalRequest();
        req.setTropelId(tropelId);
        req.setGuardianId(guardianId);
        req.setSenderTag("sensor-test");
        req.setRawContent(rawContent);
        return req;
    }

    private TropelSignal buildFakeSignal(String signalType, String severity,
                                          String assignedUnit, String recommendedAction,
                                          String status) {
        TropelSignal s = new TropelSignal();
        s.setId(1L);
        s.setTropel(tropel);
        s.setGuardian(guardian);
        s.setSenderTag("sensor-test");
        s.setRawContent("contenido de prueba");
        s.setSignalType(signalType);
        s.setSeverity(severity);
        s.setAssignedUnit(assignedUnit);
        s.setRecommendedAction(recommendedAction);
        s.setStatus(status);
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        return s;
    }
}
