package com.tuckersoft.tropelcare.repository;

import com.tuckersoft.tropelcare.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findBySignalId(Long signalId); // ← AÑADIDO
}