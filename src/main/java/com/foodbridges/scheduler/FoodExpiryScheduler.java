package com.foodbridges.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.service.EmailService;

@Service
public class FoodExpiryScheduler {

	private final FoodRepository foodRepository;

	private final EmailService emailService;

	public FoodExpiryScheduler(FoodRepository foodRepository, EmailService emailService) {
	    this.foodRepository = foodRepository;
	    this.emailService = emailService;
	}


	@Scheduled(fixedRate = 120000)
	public void expireFoods() {

		List<Food> toExpire = foodRepository.findByStatusAndExpiryTimeBefore(FoodStatus.AVAILABLE, LocalDateTime.now());

		if (toExpire.isEmpty())
			return;

		for (Food food : toExpire) {
			food.setStatus(FoodStatus.EXPIRED);
			foodRepository.save(food);

			// ✅ Expiry email
			emailService.sendEmail(
			    "donor@example.com",
			    "Food Expired",
			    "Your food '" + food.getFoodName() +
			    "' has expired and is no longer available."
			);

		}

		foodRepository.saveAll(toExpire);
		System.out.println("✅ Auto-expired foods: " + toExpire.size());
	}
}
