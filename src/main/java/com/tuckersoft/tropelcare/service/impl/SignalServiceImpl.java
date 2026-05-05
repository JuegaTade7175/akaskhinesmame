package com.tuckersoft.tropelcare.service.impl;

import com.tuckersoft.tropelcare.dto.request.CreateSignalRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.SignalResponse;
import com.tuckersoft.tropelcare.entity.*;
import com.tuckersoft.tropelcare.event.TropelSignalCreatedEvent;
import com.tuckersoft.tropelcare.exception.BadRequestException;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.*;
import com.tuckersoft.tropelcare.service.SignalService;
import com.tuckersoft.tropelcare.util.GithubModelsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalServiceImpl implements SignalService {

    private final TropelRepository tropelRepository;
    private final GuardianRepository guardianRepository;
    private final TropelSignalRepository signalRepository;
    private final CareResponseRepository careResponseRepository;
    private final SectorRepository sectorRepository;
    private final GithubModelsClient githubModelsClient;
    private final ApplicationEventPublisher eventPublisher;

    private static final Map<String, String> RESPONSE_CODE_MAP = Map.of(
            "HAMBRE",               "DISPATCH_NUTRIENT_PACK",
            "ABANDONO",             "SEND_COMPANIONSHIP_PROTOCOL",
            "MUTACION",             "ISOLATE_AND_OBSERVE",
            "FUGA",                 "ACTIVATE_SECTOR_LOCK",
            "CONFLICTO",            "DEPLOY_MEDIATION_FIELD",
            "REPRODUCCION_MASIVA",  "ENABLE_POPULATION_CONTROL",
            "SENAL_CORRUPTA",       "ARCHIVE_AND_IGNORE"
    );

    @Override
    @Transactional
    public SignalResponse create(CreateSignalRequest req) {
        Tropel tropel = tropelRepository.findById(req.getTropelId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un Tropel con id " + req.getTropelId()));
        Guardian guardian = guardianRepository.findById(req.getGuardianId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un guardián con id " + req.getGuardianId()));

        if (!tropel.getGuardian().getId().equals(req.getGuardianId())) {
            throw new BadRequestException("El guardianId no corresponde al guardián responsable de este Tropel");
        }

        Optional<GithubModelsClient.ClassificationResult> aiResult = githubModelsClient.classify(req.getRawContent());

        Instant now = Instant.now();

        if (aiResult.isEmpty()) {
            TropelSignal signal = buildSignal(req, tropel, guardian,
                    "SENAL_CORRUPTA", "LEVE", "Archivo de Senales",
                    "Archivar la señal y revisar manualmente si se repite.",
                    "ERROR", null, now);
            signalRepository.save(signal);
            createCareResponse(signal, "SENAL_CORRUPTA", "Archivar la señal y revisar manualmente si se repite.", now);
            return toResponse(signal);
        }

        GithubModelsClient.ClassificationResult result = aiResult.get();

        updateTropelStats(tropel, result.severity());

        updateSectorStability(tropel.getSector(), result.signalType());

        tropelRepository.save(tropel);
        sectorRepository.save(tropel.getSector());

        TropelSignal signal = buildSignal(req, tropel, guardian,
                result.signalType(), result.severity(), result.assignedUnit(),
                result.recommendedAction(), "RECIBIDA", result.personalityNote(), now);
        signalRepository.save(signal);

        createCareResponse(signal, result.signalType(), result.recommendedAction(), now);

        eventPublisher.publishEvent(new TropelSignalCreatedEvent(this, signal.getId()));

        log.info("[TROPEL-LOG] Thread: {} - Señal {} creada exitosamente", Thread.currentThread().getName(), signal.getId());

        return toResponse(signal);
    }

    private TropelSignal buildSignal(CreateSignalRequest req, Tropel tropel, Guardian guardian,
                                     String signalType, String severity, String assignedUnit,
                                     String recommendedAction, String status, String personalityNote, Instant now) {
        TropelSignal signal = new TropelSignal();
        signal.setTropel(tropel);
        signal.setGuardian(guardian);
        signal.setSenderTag(req.getSenderTag());
        signal.setRawContent(req.getRawContent());
        signal.setSignalType(signalType);
        signal.setSeverity(severity);
        signal.setAssignedUnit(assignedUnit);
        signal.setRecommendedAction(recommendedAction);
        signal.setStatus(status);
        signal.setPersonalityNote(personalityNote);
        signal.setCreatedAt(now);
        signal.setUpdatedAt(now);
        return signal;
    }

    private void createCareResponse(TropelSignal signal, String signalType, String description, Instant now) {
        CareResponse care = new CareResponse();
        care.setSignal(signal);
        care.setResponseCode(RESPONSE_CODE_MAP.getOrDefault(signalType, "ARCHIVE_AND_IGNORE"));
        care.setDescription(description);
        care.setCreatedAt(now);
        careResponseRepository.save(care);
    }

    private void updateTropelStats(Tropel tropel, String severity) {
        int energyDelta = switch (severity) {
            case "LEVE" -> -5;
            case "MODERADO" -> -10;
            case "GRAVE" -> -20;
            case "CRITICO" -> -30;
            default -> 0;
        };
        int chaosDelta = switch (severity) {
            case "LEVE" -> 5;
            case "MODERADO" -> 15;
            case "GRAVE" -> 30;
            case "CRITICO" -> 45;
            default -> 0;
        };
        int mutationDelta = "CRITICO".equals(severity) ? 1 : 0;

        tropel.setEnergyLevel(Math.max(0, Math.min(100, tropel.getEnergyLevel() + energyDelta)));
        tropel.setChaosIndex(Math.max(0, Math.min(100, tropel.getChaosIndex() + chaosDelta)));
        tropel.setMutationStage(Math.min(5, tropel.getMutationStage() + mutationDelta));

        if (tropel.getChaosIndex() >= 80) {
            tropel.setVitalState("CRITICO");
        } else if (tropel.getEnergyLevel() <= 20) {
            tropel.setVitalState("HAMBRIENTO");
        } else if ("CRITICO".equals(severity)) {
            tropel.setVitalState("MUTANDO");
        } else if ("GRAVE".equals(severity)) {
            tropel.setVitalState("AGITADO");
        }
        tropel.setUpdatedAt(Instant.now());
    }

    private void updateSectorStability(Sector sector, String signalType) {
        if ("FUGA".equals(signalType)) {
            sector.setStabilityLevel(Math.max(0, sector.getStabilityLevel() - 10));
        } else if ("REPRODUCCION_MASIVA".equals(signalType)) {
            sector.setStabilityLevel(Math.max(0, sector.getStabilityLevel() - 15));
        }
    }

    @Override
    public PagedResponse<SignalResponse> findAll(Long tropelId, String signalType, String status, Pageable pageable) {
        Page<TropelSignal> page = signalRepository.findWithFilters(tropelId, signalType, status, pageable);
        return PagedResponse.<SignalResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .build();
    }

    @Override
    public SignalResponse findById(Long id) {
        return toResponse(signalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una señal con id " + id)));
    }

    private SignalResponse toResponse(TropelSignal s) {
        return SignalResponse.builder()
                .id(s.getId())
                .tropelId(s.getTropel().getId())
                .tropelName(s.getTropel().getName())
                .guardianId(s.getGuardian().getId())
                .guardianName(s.getGuardian().getDisplayName())
                .senderTag(s.getSenderTag())
                .rawContent(s.getRawContent())
                .signalType(s.getSignalType())
                .severity(s.getSeverity())
                .assignedUnit(s.getAssignedUnit())
                .recommendedAction(s.getRecommendedAction())
                .status(s.getStatus())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}