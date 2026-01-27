package com.foodbridges.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodbridges.entity.NightPickup;

public interface NightPickupRepository extends JpaRepository<NightPickup, Long> {

    // already used in NightPickupService
    Optional<NightPickup> findByFoodId(Long foodId);

    // ✅ ADD THIS (for Admin face-verified map)
    List<NightPickup> findByFoodIdIn(List<Long> foodIds);
}
