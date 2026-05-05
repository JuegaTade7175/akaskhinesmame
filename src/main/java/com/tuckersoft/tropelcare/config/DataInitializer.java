package com.tuckersoft.tropelcare.config;

import com.tuckersoft.tropelcare.entity.Guardian;
import com.tuckersoft.tropelcare.repository.GuardianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final GuardianRepository guardianRepository;

    @Value("${app.admin.display-name}")
    private String adminName;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.notification-email}")
    private String adminNotificationEmail;

    @Override
    public void run(ApplicationArguments args) {
        if (guardianRepository.existsByEmail(adminEmail)) {
            log.info("[TROPEL-LOG] Guardian Cameron Walker ya existe. Omitiendo inicialización.");
            return;
        }
        Guardian cameron = new Guardian();
        cameron.setDisplayName(adminName);
        cameron.setEmail(adminEmail);
        cameron.setNotificationEmail(adminNotificationEmail);
        cameron.setCreatedAt(Instant.now());
        guardianRepository.save(cameron);
        log.info("[TROPEL-LOG] Guardian '{}' creado exitosamente.", adminName);
    }
}