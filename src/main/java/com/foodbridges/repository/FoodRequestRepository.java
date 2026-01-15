package com.foodbridges.repository;

import com.foodbridges.entity.FoodRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {
    List<FoodRequest> findByReceiverId(Long receiverId);
    List<FoodRequest> findByFoodId(Long foodId);
}
