package com.tuckersoft.tropelcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "sectors")
@Getter
@Setter
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sectorCode;

    @Column(nullable = false)
    private String climate;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentLoad = 0;

    @Column(nullable = false)
    private Integer stabilityLevel = 100;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "sector")
    private List<Tropel> tropels;
}