package com.tuckersoft.tropelcare.listener;

import com.tuckersoft.tropelcare.entity.NotificationLog;
import com.tuckersoft.tropelcare.entity.TropelSignal;
import com.tuckersoft.tropelcare.event.TropelSignalCreatedEvent;
import com.tuckersoft.tropelcare.repository.NotificationLogRepository;
import com.tuckersoft.tropelcare.repository.TropelSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TropelSignalNotificationListener {

    private final TropelSignalRepository signalRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final JavaMailSender mailSender;

    @Async("tropelTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSignalCreated(TropelSignalCreatedEvent event) {

        TropelSignal signal = signalRepository.findById(event.getSignalId()).orElse(null);
        if (signal == null) {
            log.error("[TROPEL-LOG] Señal {} no encontrada para notificación", event.getSignalId());
            return;
        }

        signal.setStatus("PROCESANDO");
        signal.setUpdatedAt(Instant.now());
        signalRepository.save(signal);

        String tropelName = signal.getTropel().getName();
        String recipientEmail = signal.getGuardian().getNotificationEmail();

        String subject = String.format("[TROPELCARE] %s detectada en %s | Severidad %s",
                signal.getSignalType(),
                tropelName,
                signal.getSeverity());

        NotificationLog notifLog = new NotificationLog();
        notifLog.setSignal(signal);
        notifLog.setRecipientEmail(recipientEmail);
        notifLog.setSubject(subject);
        notifLog.setCreatedAt(Instant.now());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(buildEmailBody(signal));
            mailSender.send(message);

            signal.setStatus("ATENDIDA");
            signal.setUpdatedAt(Instant.now());
            signalRepository.save(signal);

            notifLog.setNotifStatus("SENT");
            notifLog.setSentAt(Instant.now());

        } catch (Exception e) {
            signal.setStatus("ERROR");
            signal.setUpdatedAt(Instant.now());
            signalRepository.save(signal);

            notifLog.setNotifStatus("FAILED");
            notifLog.setErrorMessage(e.getMessage());
            log.error("[TROPEL-LOG] Fallo al enviar email para señal {}: {}",
                    signal.getId(), e.getMessage());
        }

        notificationLogRepository.save(notifLog);

        log.info("[TROPEL-LOG] Signal ID: {} | Tropel: {} | Type: {} | Severity: {} | Unit: {} | Thread: {} | Status: {}",
                signal.getId(),
                tropelName,
                signal.getSignalType(),
                signal.getSeverity(),
                signal.getAssignedUnit(),
                Thread.currentThread().getName(),
                signal.getStatus());
    }

    private String buildEmailBody(TropelSignal signal) {
        return String.format("""
                Hola %s,
                
                Tu Tropel ha emitido una señal que requiere atención.
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Señal ID         : #%d
                Tropel           : %s (%s)
                Tipo de señal    : %s
                Severidad        : %s
                Unidad asignada  : %s
                Acción sugerida  : %s
                Estado vital     : %s
                Nivel de energía : %d/100
                Índice de caos   : %d/100
                Etapa mutación   : %d/5
                Registrada       : %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                Señal original:
                "%s"
                
                — TropelCare Signal Engine, Tuckersoft
                """,
                signal.getGuardian().getDisplayName(),
                signal.getId(),
                signal.getTropel().getName(),
                signal.getTropel().getSpecies(),
                signal.getSignalType(),
                signal.getSeverity(),
                signal.getAssignedUnit(),
                signal.getRecommendedAction(),
                signal.getTropel().getVitalState(),
                signal.getTropel().getEnergyLevel(),
                signal.getTropel().getChaosIndex(),
                signal.getTropel().getMutationStage(),
                signal.getCreatedAt(),
                signal.getRawContent());
    }
}