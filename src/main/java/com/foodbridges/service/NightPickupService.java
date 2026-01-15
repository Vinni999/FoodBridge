package com.foodbridges.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.entity.NightPickup;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.repository.NightPickupRepository;

@Service
public class NightPickupService {

    private final NightPickupRepository nightPickupRepository;
    private final FoodRepository foodRepository;

    public NightPickupService(NightPickupRepository nightPickupRepository,
                              FoodRepository foodRepository) {
        this.nightPickupRepository = nightPickupRepository;
        this.foodRepository = foodRepository;
    }

    @Transactional
    public String verifyPickupPin(Long foodId, String enteredPin) {

        NightPickup pickup = nightPickupRepository.findByFoodId(foodId)
                .orElseThrow(() -> new RuntimeException("Pickup record not found for this food"));

        if (pickup.isUsed()) {
            throw new RuntimeException("Food already picked up");
        }

        if (pickup.getPin() == null || !pickup.getPin().equals(enteredPin)) {
            throw new RuntimeException("Invalid PIN");
        }

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found"));

        if (food.getStatus() != FoodStatus.AVAILABLE &&
            food.getStatus() != FoodStatus.REQUESTED) {
            throw new RuntimeException("Food not available for pickup");
        }

        if (food.getExpiryTime() != null && food.getExpiryTime().isBefore(LocalDateTime.now())) {
            food.setStatus(FoodStatus.EXPIRED);
            foodRepository.save(food);
            throw new RuntimeException("Food expired");
        }

        pickup.setVerified(true);
        pickup.setUsed(true);
        pickup.setUsedAt(LocalDateTime.now());
        nightPickupRepository.save(pickup);

        food.setStatus(FoodStatus.PICKED);
        foodRepository.save(food);

        return "✅ PIN verified. Food picked successfully.";
    }
}
