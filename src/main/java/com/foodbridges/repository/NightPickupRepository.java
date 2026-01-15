package com.foodbridges.repository;

import com.foodbridges.entity.NightPickup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NightPickupRepository extends JpaRepository<NightPickup, Long> {

    Optional<NightPickup> findByFoodId(Long foodId);

}
