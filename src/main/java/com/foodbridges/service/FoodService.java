package com.foodbridges.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.foodbridges.dto.FoodRequest;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.repository.FoodRepository;

@Service
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public Food createFood(FoodRequest request) {
        Food food = new Food();
        food.setFoodName(request.getFoodName());
        food.setQuantity(request.getQuantity());
        food.setPickupLocation(request.getPickupLocation());
        food.setExpiryTime(request.getExpiryTime());
        food.setDonorId(request.getDonorId());
        food.setNightPickup(request.isNightPickup());

        food.setStatus(FoodStatus.AVAILABLE);
        food.setCreatedAt(LocalDateTime.now());

        return foodRepository.save(food);
    }

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }
}