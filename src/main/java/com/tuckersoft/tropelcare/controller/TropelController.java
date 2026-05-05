package com.tuckersoft.tropelcare.controller;

import com.tuckersoft.tropelcare.dto.request.CreateTropelRequest;
import com.tuckersoft.tropelcare.dto.response.PagedResponse;
import com.tuckersoft.tropelcare.dto.response.TropelResponse;
import com.tuckersoft.tropelcare.service.TropelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tropels")
@RequiredArgsConstructor
public class TropelController {

    private final TropelService tropelService;

    @PostMapping
    public ResponseEntity<TropelResponse> create(@Valid @RequestBody CreateTropelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tropelService.create(request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TropelResponse>> findAll(
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String vitalState,
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) Long guardianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tropelService.findAll(species, vitalState, sectorId, guardianId,
                PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TropelResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tropelService.findById(id));
    }
}