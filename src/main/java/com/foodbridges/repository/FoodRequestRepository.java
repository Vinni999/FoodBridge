package com.foodbridges.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodbridges.entity.FoodRequest;
import com.foodbridges.entity.RequestStatus;

public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {

    // existing methods (keep)
    List<FoodRequest> findByReceiverId(Long receiverId);
    List<FoodRequest> findByFoodId(Long foodId);
    long countByStatus(RequestStatus status);
    java.util.List<com.foodbridges.entity.FoodRequest> findTop10ByOrderByIdDesc();

    // ✅ NEW: get latest request for a food (fixes your service error)
    Optional<FoodRequest> findTopByFoodIdOrderByIdDesc(Long foodId);
}
