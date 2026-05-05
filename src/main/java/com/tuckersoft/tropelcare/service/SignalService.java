package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.request.CreateSignalRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.SignalResponse;
import org.springframework.data.domain.Pageable;

public interface SignalService {
    SignalResponse create(CreateSignalRequest request);

    PagedResponse<SignalResponse> findAll(Long tropelId, String signalType, String severity,
                                          Long guardianId, String status, Pageable pageable);

    SignalResponse findById(Long id);
}