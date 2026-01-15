package com.foodbridges.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.repository.FoodRepository;

@Service
public class FoodExpiryScheduler {

	private final FoodRepository foodRepository;

	public FoodExpiryScheduler(FoodRepository foodRepository) {
		this.foodRepository = foodRepository;
	}

	@Scheduled(fixedRate = 120000)
	public void expireFoods() {

		List<Food> toExpire = foodRepository.findByStatusAndExpiryTimeBefore(FoodStatus.AVAILABLE, LocalDateTime.now());

		if (toExpire.isEmpty())
			return;

		for (Food food : toExpire) {
			food.setStatus(FoodStatus.EXPIRED);
		}

		foodRepository.saveAll(toExpire);
		System.out.println("✅ Auto-expired foods: " + toExpire.size());
	}
}
