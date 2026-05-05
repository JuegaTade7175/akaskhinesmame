package com.tuckersoft.tropelcare.repository;

import com.tuckersoft.tropelcare.entity.TropelSignal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TropelSignalRepository extends JpaRepository<TropelSignal, Long> {

    @Query("SELECT s FROM TropelSignal s WHERE " +
           "(:tropelId IS NULL OR s.tropel.id = :tropelId) AND " +
           "(:signalType IS NULL OR s.signalType = :signalType) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<TropelSignal> findWithFilters(
            @Param("tropelId") Long tropelId,
            @Param("signalType") String signalType,
            @Param("status") String status,
            Pageable pageable);

    List<TropelSignal> findByTropelIdAndPersonalityNoteIsNotNullOrderByCreatedAtDesc(Long tropelId);
}