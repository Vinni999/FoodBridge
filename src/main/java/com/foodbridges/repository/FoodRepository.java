package com.foodbridges.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;

public interface FoodRepository extends JpaRepository<Food, Long> {
	List<Food> findByStatus(FoodStatus status);

	List<Food> findByDonorId(Long donorId);

	List<Food> findByStatusAndExpiryTimeBefore(FoodStatus status, LocalDateTime time);
}
