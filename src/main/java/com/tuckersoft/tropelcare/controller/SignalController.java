package com.tuckersoft.tropelcare.controller;

import com.tuckersoft.tropelcare.dto.response.CareResponseDto;
import com.tuckersoft.tropelcare.dto.response.NotificationLogDto;
import com.tuckersoft.tropelcare.dto.request.CreateSignalRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.SignalResponse;
import com.tuckersoft.tropelcare.entity.CareResponse;
import com.tuckersoft.tropelcare.entity.NotificationLog;
import com.tuckersoft.tropelcare.entity.TropelSignal;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.CareResponseRepository;
import com.tuckersoft.tropelcare.repository.NotificationLogRepository;
import com.tuckersoft.tropelcare.repository.TropelSignalRepository;
import com.tuckersoft.tropelcare.service.SignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/signals")
@RequiredArgsConstructor
public class SignalController {

    private final SignalService signalService;
    private final CareResponseRepository careResponseRepository;       
    private final NotificationLogRepository notificationLogRepository; 
    private final TropelSignalRepository signalRepository;            

    @PostMapping
    public ResponseEntity<SignalResponse> create(@Valid @RequestBody CreateSignalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signalService.create(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<SignalResponse>> findAll(
            @RequestParam(required = false) Long tropelId,
            @RequestParam(required = false) String signalType,
            @RequestParam(required = false) String severity,     
            @RequestParam(required = false) Long guardianId,     
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                signalService.findAll(tropelId, signalType, severity, guardianId, status, PageRequest.of(page, size))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SignalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(signalService.findById(id));
    }

    @GetMapping("/{id}/care-response")
    public ResponseEntity<CareResponseDto> getCareResponse(@PathVariable Long id) {
        if (!signalRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe una señal con id " + id);
        }
        CareResponse care = careResponseRepository.findBySignalId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una respuesta de cuidado para la señal #" + id));

        return ResponseEntity.ok(CareResponseDto.builder()
                .id(care.getId())
                .signalId(id)
                .responseCode(care.getResponseCode())
                .description(care.getDescription())
                .createdAt(care.getCreatedAt())
                .build());
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<List<NotificationLogDto>> getNotifications(@PathVariable Long id) {
        if (!signalRepository.existsById(id)) {
            throw new ResourceNotFoundException("No existe una señal con id " + id);
        }
        List<NotificationLog> logs = notificationLogRepository.findBySignalId(id);

        List<NotificationLogDto> result = logs.stream()
                .map(log -> NotificationLogDto.builder()
                        .id(log.getId())
                        .signalId(id)
                        .recipientEmail(log.getRecipientEmail())
                        .subject(log.getSubject())
                        .notifStatus(log.getNotifStatus())
                        .errorMessage(log.getErrorMessage())
                        .sentAt(log.getSentAt())
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}