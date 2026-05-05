package com.tuckersoft.tropelcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tropel_signals")
@Getter
@Setter
public class TropelSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tropel_id", nullable = false)
    private Tropel tropel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Column(nullable = false)
    private String senderTag;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawContent;

    private String signalType;
    private String severity;
    private String assignedUnit;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String personalityNote;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToOne(mappedBy = "signal")
    private CareResponse careResponse;

    @OneToMany(mappedBy = "signal")
    private List<NotificationLog> notificationLogs;
}