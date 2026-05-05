package com.tuckersoft.tropelcare.repository;

import com.tuckersoft.tropelcare.entity.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    boolean existsBySectorCode(String sectorCode);
}