package com.tuckersoft.tropelcare.controller;

import com.tuckersoft.tropelcare.dto.response.DiaryResponse;
import com.tuckersoft.tropelcare.entity.Tropel;
import com.tuckersoft.tropelcare.entity.TropelSignal;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.TropelRepository;
import com.tuckersoft.tropelcare.repository.TropelSignalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tropels")
@RequiredArgsConstructor
public class TropelDiaryController {

    private final TropelRepository tropelRepository;
    private final TropelSignalRepository signalRepository;

    @GetMapping("/{id}/diary")
    public ResponseEntity<DiaryResponse> getDiary(@PathVariable Long id) {
        Tropel tropel = tropelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un Tropel con id " + id));

        List<TropelSignal> signals =
                signalRepository.findByTropelIdAndPersonalityNoteIsNotNullOrderByCreatedAtDesc(id);

        List<DiaryResponse.DiaryNoteResponse> notes = signals.stream()
                .map(s -> DiaryResponse.DiaryNoteResponse.builder()
                        .signalId(s.getId())
                        .personalityNote(s.getPersonalityNote())
                        .createdAt(s.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(DiaryResponse.builder()
                .tropelId(tropel.getId())
                .tropelName(tropel.getName())
                .notes(notes)
                .build());
    }
}