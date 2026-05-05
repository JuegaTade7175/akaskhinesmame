package com.tuckersoft.tropelcare.repository;

import com.tuckersoft.tropelcare.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}