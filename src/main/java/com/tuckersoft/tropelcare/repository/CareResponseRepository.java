package com.tuckersoft.tropelcare.repository;

import com.tuckersoft.tropelcare.entity.CareResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareResponseRepository extends JpaRepository<CareResponse, Long> {
    Optional<CareResponse> findBySignalId(Long signalId); // ← AÑADIDO
}