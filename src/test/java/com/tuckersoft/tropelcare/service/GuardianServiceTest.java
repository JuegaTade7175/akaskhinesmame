package com.tuckersoft.tropelcare.service;

import com.tuckersoft.tropelcare.dto.response.GuardianResponse;
import com.tuckersoft.tropelcare.entity.Guardian;
import com.tuckersoft.tropelcare.exception.ResourceNotFoundException;
import com.tuckersoft.tropelcare.repository.GuardianRepository;
import com.tuckersoft.tropelcare.service.impl.GuardianServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuardianServiceTest {

    @Mock
    private GuardianRepository guardianRepository;

    @InjectMocks
    private GuardianServiceImpl guardianService;

    @Test
    void findById_existingGuardian_returnsResponse() {
        Guardian guardian = new Guardian();
        guardian.setId(1L);
        guardian.setDisplayName("Cameron Walker");
        guardian.setEmail("cameron@tuckersoft.com");
        guardian.setNotificationEmail("cam.real@gmail.com");
        guardian.setCreatedAt(Instant.now());

        when(guardianRepository.findById(1L)).thenReturn(Optional.of(guardian));

        GuardianResponse response = guardianService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDisplayName()).isEqualTo("Cameron Walker");
        assertThat(response.getEmail()).isEqualTo("cameron@tuckersoft.com");
    }

    @Test
    void findById_nonExistingGuardian_throwsNotFound() {
        when(guardianRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guardianService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
