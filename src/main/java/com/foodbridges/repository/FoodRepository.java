package com.foodbridges.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;

public interface FoodRepository extends JpaRepository<Food, Long> {

    // Existing methods (keep)
    List<Food> findByStatus(FoodStatus status);
    long countByStatus(FoodStatus status);
    java.util.List<com.foodbridges.entity.Food> findTop10ByOrderByIdDesc();

    List<Food> findByDonorId(Long donorId);

    List<Food> findByStatusAndExpiryTimeBefore(FoodStatus status, LocalDateTime time);

    List<Food> findByPickupLocationContainingIgnoreCase(String pickupLocation);
    Optional<Food> findByRequestId(Long requestId);

    // ✅ NEW: Nearby Foods (Location Matching) - Returns food + distance_km
    @Query(value = """
        SELECT
            f.id,
            f.food_name,
            f.quantity,
            f.pickup_location,
            f.expiry_time,
            f.status,
            (6371 * acos(
                cos(radians(:lat)) * cos(radians(f.latitude)) *
                cos(radians(f.longitude) - radians(:lng)) +
                sin(radians(:lat)) * sin(radians(f.latitude))
            )) AS distance_km
        FROM foods f
        WHERE f.status = 'AVAILABLE'
          AND f.expiry_time > NOW()
          AND f.latitude IS NOT NULL
          AND f.longitude IS NOT NULL
        HAVING distance_km <= :radiusKm
        ORDER BY distance_km ASC
        """, nativeQuery = true)
    List<Object[]> findNearbyFoods(@Param("lat") double lat,
                                  @Param("lng") double lng,
                                  @Param("radiusKm") double radiusKm);
}
