package com.tuckersoft.tropelcare.service.impl;

import com.tuckersoft.tropelcare.dto.response.GuardianResponse;
import com.tuckersoft.tropelcare.entity.Guardian;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.GuardianRepository;
import com.tuckersoft.tropelcare.service.GuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuardianServiceImpl implements GuardianService {

    private final GuardianRepository guardianRepository;

    @Override
    public List<GuardianResponse> findAll() {
        return guardianRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GuardianResponse findById(Long id) {
        Guardian g = guardianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un guardián con id " + id));
        return toResponse(g);
    }

    private GuardianResponse toResponse(Guardian g) {
        return GuardianResponse.builder()
                .id(g.getId())
                .displayName(g.getDisplayName())
                .email(g.getEmail())
                .notificationEmail(g.getNotificationEmail())
                .createdAt(g.getCreatedAt())
                .build();
    }
}