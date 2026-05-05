package com.tuckersoft.tropelcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "tropels")
@Getter
@Setter
public class Tropel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @Column(nullable = false)
    private String species;

    @Column(nullable = false)
    private String vitalState = "ESTABLE";

    @Column(nullable = false)
    private Integer energyLevel = 80;

    @Column(nullable = false)
    private Integer chaosIndex = 10;

    @Column(nullable = false)
    private Integer mutationStage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "tropel")
    private List<TropelSignal> signals;
}