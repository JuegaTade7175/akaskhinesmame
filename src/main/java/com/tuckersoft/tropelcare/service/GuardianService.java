package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.response.GuardianResponse;

import java.util.List;

public interface GuardianService {
    List<GuardianResponse> findAll();
    GuardianResponse findById(Long id);
}