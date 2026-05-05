package com.tuckersoft.tropelcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "care_responses")
@Getter
@Setter
public class CareResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false, unique = true)
    private TropelSignal signal;

    @Column(nullable = false)
    private String responseCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Instant createdAt;
}