package com.tuckersoft.tropelcare.controller;

import com.tuckersoft.tropelcare.dto.response.GuardianResponse;
import com.tuckersoft.tropelcare.service.GuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guardians")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;

    @GetMapping
    public ResponseEntity<List<GuardianResponse>> findAll() {
        return ResponseEntity.ok(guardianService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuardianResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(guardianService.findById(id));
    }
}