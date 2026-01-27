package com.foodbridges.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.foodbridges.dto.FoodNearbyResponse;
import com.foodbridges.dto.FoodRequest;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.repository.FoodRepository;

@Service
public class FoodService {

    private final FoodRepository foodRepository;
    private final EmailService emailService;

    public FoodService(FoodRepository foodRepository, EmailService emailService) {
        this.foodRepository = foodRepository;
        this.emailService = emailService;
    }

    /* =========================
       CREATE FOOD (Donor)
    ========================== */
    public Food createFood(FoodRequest request) {

        Food food = new Food();
        food.setFoodName(request.getFoodName());
        food.setQuantity(request.getQuantity());
        food.setPickupLocation(request.getPickupLocation());
        food.setExpiryTime(request.getExpiryTime());
        food.setDonorId(request.getDonorId());
        food.setNightPickup(request.isNightPickup());

        // ✅ GPS coordinates (for location matching)
        food.setLatitude(request.getLatitude());
        food.setLongitude(request.getLongitude());

        food.setStatus(FoodStatus.AVAILABLE);
        food.setCreatedAt(LocalDateTime.now());

        Food savedFood = foodRepository.save(food);

        // Email notification (optional – keep as is)
        emailService.sendEmail(
                "donor@example.com",
                "Food Added Successfully",
                "Your food '" + savedFood.getFoodName() + "' has been added successfully.\n\n"
                        + "Food ID: " + savedFood.getId() + "\n"
                        + "Quantity: " + savedFood.getQuantity() + "\n"
                        + "Pickup Location: " + savedFood.getPickupLocation()
        );

        return savedFood;
    }

    /* =========================
       GET ALL FOODS
    ========================== */
    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    /* =========================
       SEARCH BY LOCATION TEXT
    ========================== */
    public List<Food> searchByLocation(String location) {
        return foodRepository.findByPickupLocationContainingIgnoreCase(location);
    }

    /* =========================
       NEARBY FOODS (GPS MATCHING)
    ========================== */
    public List<FoodNearbyResponse> getNearbyFoods(double lat, double lng, double radiusKm) {

        List<Object[]> rows = foodRepository.findNearbyFoods(lat, lng, radiusKm);
        List<FoodNearbyResponse> result = new ArrayList<>();

        for (Object[] r : rows) {
            FoodNearbyResponse dto = new FoodNearbyResponse();

            dto.setId(((Number) r[0]).longValue());
            dto.setFoodName((String) r[1]);
            dto.setQuantity(((Number) r[2]).intValue());
            dto.setPickupLocation((String) r[3]);
            dto.setExpiryTime(String.valueOf(r[4]));
            dto.setStatus(String.valueOf(r[5]));
            dto.setDistanceKm(((Number) r[6]).doubleValue());

            result.add(dto);
        }

        return result;
    }
}
