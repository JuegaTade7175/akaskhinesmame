package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateSectorRequest;
import com.tuckersoft.tropelcare.dto.response.SectorResponse;

import java.util.List;

public interface SectorService {
    SectorResponse create(CreateSectorRequest request);
    List<SectorResponse> findAll();
    SectorResponse findById(Long id);
}