package com.tuckersoft.tropelcare.repository;

import com.tuckersoft.tropelcare.entity.Tropel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TropelRepository extends JpaRepository<Tropel, Long> {
    boolean existsByName(String name);

    @Query("SELECT t FROM Tropel t WHERE " +
           "(:species IS NULL OR t.species = :species) AND " +
           "(:vitalState IS NULL OR t.vitalState = :vitalState) AND " +
           "(:sectorId IS NULL OR t.sector.id = :sectorId) AND " +
           "(:guardianId IS NULL OR t.guardian.id = :guardianId)")
    Page<Tropel> findWithFilters(
            @Param("species") String species,
            @Param("vitalState") String vitalState,
            @Param("sectorId") Long sectorId,
            @Param("guardianId") Long guardianId,
            Pageable pageable);
}