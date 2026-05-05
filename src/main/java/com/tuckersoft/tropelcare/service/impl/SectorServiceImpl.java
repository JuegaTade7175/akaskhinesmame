package com.tuckersoft.tropelcare.service.impl;

import com.tuckersoft.tropelcare.dto.request.CreateSectorRequest;
import com.tuckersoft.tropelcare.dto.response.SectorResponse;
import com.tuckersoft.tropelcare.entity.Sector;
import com.tuckersoft.tropelcare.exception.BadRequestException;
import com.tuckersoft.tropelcare.exception.ConflictException;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.SectorRepository;
import com.tuckersoft.tropelcare.service.SectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {

    private final SectorRepository sectorRepository;

    @Override
    @Transactional
    public SectorResponse create(CreateSectorRequest req) {
        if (sectorRepository.existsBySectorCode(req.getSectorCode())) {
            throw new ConflictException("Ya existe un sector con código " + req.getSectorCode());
        }
        if (req.getCapacity() == null || req.getCapacity() <= 0) {
            throw new BadRequestException("La capacidad debe ser mayor a 0");
        }
        Sector sector = new Sector();
        sector.setSectorCode(req.getSectorCode());
        sector.setClimate(req.getClimate());
        sector.setCapacity(req.getCapacity());
        sector.setCurrentLoad(0);
        sector.setStabilityLevel(100);
        sector.setCreatedAt(Instant.now());
        return toResponse(sectorRepository.save(sector));
    }

    @Override
    public List<SectorResponse> findAll() {
        return sectorRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public SectorResponse findById(Long id) {
        return toResponse(sectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un sector con id " + id)));
    }

    private SectorResponse toResponse(Sector s) {
        return SectorResponse.builder()
                .id(s.getId())
                .sectorCode(s.getSectorCode())
                .climate(s.getClimate())
                .capacity(s.getCapacity())
                .currentLoad(s.getCurrentLoad())
                .stabilityLevel(s.getStabilityLevel())
                .createdAt(s.getCreatedAt())
                .build();
    }
}