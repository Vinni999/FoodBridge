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

    // runs every 2 minutes
    @Scheduled(fixedRate = 120000)
    public void expireFoods() {

        List<Food> toExpire =
                foodRepository.findByStatusAndExpiryTimeBefore(
                        FoodStatus.AVAILABLE,
                        LocalDateTime.now()
                );

        if (toExpire.isEmpty()) {
            return;
        }

        for (Food food : toExpire) {
            // ✅ update status
            food.setStatus(FoodStatus.EXPIRED);
            foodRepository.save(food);

            // ✅ SAFE email sending (won't break scheduler)
            try {
                emailService.sendEmail(
                        "donor@example.com",
                        "Food Expired",
                        "Your food '" + food.getFoodName() +
                        "' has expired and is no longer available."
                );
            } catch (Exception e) {
                System.out.println(
                    "📧 Email skipped (SMTP / rate limit issue): " + e.getMessage()
                );
            }
        }

        System.out.println("✅ Auto-expired foods: " + toExpire.size());
    }
}
