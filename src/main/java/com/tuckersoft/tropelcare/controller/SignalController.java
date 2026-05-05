package com.tuckersoft.tropelcare.controller;

import com.tuckersoft.tropelcare.dto.request.CreateSignalRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.SignalResponse;
import com.tuckersoft.tropelcare.service.SignalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/signals")
@RequiredArgsConstructor
public class SignalController {

    private final SignalService signalService;

    @PostMapping
    public ResponseEntity<SignalResponse> create(@Valid @RequestBody CreateSignalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signalService.create(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<SignalResponse>> findAll(
            @RequestParam(required = false) Long tropelId,
            @RequestParam(required = false) String signalType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(signalService.findAll(tropelId, signalType, status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SignalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(signalService.findById(id));
    }
}