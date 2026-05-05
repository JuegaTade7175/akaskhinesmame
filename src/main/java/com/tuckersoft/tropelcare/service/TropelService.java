package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateTropelRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.TropelResponse;
import org.springframework.data.domain.Pageable;

public interface TropelService {
    TropelResponse create(CreateTropelRequest request);
    PagedResponse<TropelResponse> findAll(String species, String vitalState, Long sectorId, Long guardianId, Pageable pageable);
    TropelResponse findById(Long id);
}