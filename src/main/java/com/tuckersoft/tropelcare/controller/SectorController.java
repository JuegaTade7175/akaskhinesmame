package com.tuckersoft.tropelcare.controller;

import com.tuckersoft.tropelcare.dto.request.CreateSectorRequest;
import com.tuckersoft.tropelcare.dto.response.SectorResponse;
import com.tuckersoft.tropelcare.service.SectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sectors")
@RequiredArgsConstructor
public class SectorController {

    private final SectorService sectorService;

    @PostMapping
    public ResponseEntity<SectorResponse> create(@Valid @RequestBody CreateSectorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectorService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SectorResponse>> findAll() {
        return ResponseEntity.ok(sectorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectorResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(sectorService.findById(id));
    }
}