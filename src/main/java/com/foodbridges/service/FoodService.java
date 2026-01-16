package com.foodbridges.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.foodbridges.dto.FoodRequest;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.service.EmailService;

@Service
public class FoodService {

	private final FoodRepository foodRepository;

	private final EmailService emailService;

	public FoodService(FoodRepository foodRepository, EmailService emailService) {
		this.foodRepository = foodRepository;
		this.emailService = emailService;
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
		Food savedFood = foodRepository.save(food);

		emailService.sendEmail("donor@example.com", 
				"Food Added Successfully",
				"Your food '" + savedFood.getFoodName() + "' has been added successfully.\n\n" + "Food ID: "
						+ savedFood.getId() + "\n" + "Quantity: " + savedFood.getQuantity() + "\n" + "Pickup Location: "
						+ savedFood.getPickupLocation());

		return savedFood;
	}

	public List<Food> getAllFoods() {
		return foodRepository.findAll();
	}
}