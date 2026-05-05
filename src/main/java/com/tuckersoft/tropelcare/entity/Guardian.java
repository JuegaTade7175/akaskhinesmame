package com.tuckersoft.tropelcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "guardians")
@Getter
@Setter
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String displayName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String notificationEmail;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "guardian")
    private List<Tropel> tropels;
}