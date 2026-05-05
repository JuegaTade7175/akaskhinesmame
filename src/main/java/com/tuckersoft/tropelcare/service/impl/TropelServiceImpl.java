package com.tuckersoft.tropelcare.service.impl;

import com.tuckersoft.tropelcare.dto.request.CreateTropelRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.TropelResponse;
import com.tuckersoft.tropelcare.entity.Guardian;
import com.tuckersoft.tropelcare.entity.Sector;
import com.tuckersoft.tropelcare.entity.Tropel;
import com.tuckersoft.tropelcare.exception.BadRequestException;
import com.tuckersoft.tropelcare.exception.ConflictException;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.GuardianRepository;
import com.tuckersoft.tropelcare.repository.SectorRepository;
import com.tuckersoft.tropelcare.repository.TropelRepository;
import com.tuckersoft.tropelcare.service.TropelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TropelServiceImpl implements TropelService {

    private final TropelRepository tropelRepository;
    private final SectorRepository sectorRepository;
    private final GuardianRepository guardianRepository;

    @Override
    @Transactional
    public TropelResponse create(CreateTropelRequest req) {
        Sector sector = sectorRepository.findById(req.getSectorId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un sector con id " + req.getSectorId()));
        Guardian guardian = guardianRepository.findById(req.getGuardianId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un guardián con id " + req.getGuardianId()));

        if (sector.getCurrentLoad() >= sector.getCapacity()) {
            throw new BadRequestException("El sector está lleno (capacidad: " + sector.getCapacity() + ")");
        }
        if (tropelRepository.existsByName(req.getName())) {
            throw new ConflictException("Ya existe un Tropel con el nombre " + req.getName());
        }

        Instant now = Instant.now();
        Tropel tropel = new Tropel();
        tropel.setName(req.getName());
        tropel.setSpecies(req.getSpecies());
        tropel.setVitalState("ESTABLE");
        tropel.setEnergyLevel(80);
        tropel.setChaosIndex(10);
        tropel.setMutationStage(0);
        tropel.setSector(sector);
        tropel.setGuardian(guardian);
        tropel.setCreatedAt(now);
        tropel.setUpdatedAt(now);

        sector.setCurrentLoad(sector.getCurrentLoad() + 1);
        sectorRepository.save(sector);

        return toResponse(tropelRepository.save(tropel));
    }

    @Override
    public PagedResponse<TropelResponse> findAll(String species, String vitalState, Long sectorId, Long guardianId, Pageable pageable) {
        Page<Tropel> page = tropelRepository.findWithFilters(species, vitalState, sectorId, guardianId, pageable);
        return PagedResponse.<TropelResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()))
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .build();
    }

    @Override
    public TropelResponse findById(Long id) {
        return toResponse(tropelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un Tropel con id " + id)));
    }

    public TropelResponse toResponse(Tropel t) {
        return TropelResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .species(t.getSpecies())
                .vitalState(t.getVitalState())
                .energyLevel(t.getEnergyLevel())
                .chaosIndex(t.getChaosIndex())
                .mutationStage(t.getMutationStage())
                .sectorId(t.getSector().getId())
                .sectorCode(t.getSector().getSectorCode())
                .guardianId(t.getGuardian().getId())
                .guardianName(t.getGuardian().getDisplayName())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}