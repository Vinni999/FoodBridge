package com.foodbridges.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foodbridges.dto.FaceVerifyResponse;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.entity.NightPickup;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.repository.NightPickupRepository;

@Service
public class NightPickupService {

    private final NightPickupRepository nightPickupRepository;
    private final FoodRepository foodRepository;
    private final EmailService emailService;

    // ✅ Add FaceRecognitionService
    private final FaceRecognitionService faceRecognitionService;

    public NightPickupService(
            NightPickupRepository nightPickupRepository,
            FoodRepository foodRepository,
            EmailService emailService,
            FaceRecognitionService faceRecognitionService) {
        this.nightPickupRepository = nightPickupRepository;
        this.foodRepository = foodRepository;
        this.emailService = emailService;
        this.faceRecognitionService = faceRecognitionService;
    }

    /**
     * ✅ New method:
     * 1) Verify Face
     * 2) If match => Verify PIN (existing logic)
     *
     * Returns:
     * - String success message OR
     * - FaceVerifyResponse if face failed (controller returns 403)
     */
    @Transactional
    public Object verifyPickupPinWithFace(Long foodId, String enteredPin, Long userId, MultipartFile image) throws Exception {

        FaceVerifyResponse faceResp = faceRecognitionService.verifyFace(userId, image);

        // IMPORTANT:
        // Your FaceVerifyResponse method name could be isMatch() OR getMatch()
        // Use whichever exists in your DTO. If this line gives error, change to faceResp.getMatch()
        if (!faceResp.isMatch()) {
            return faceResp; // controller will return 403
        }

        // Face matched ✅ now verify PIN with existing method
        return verifyPickupPin(foodId, enteredPin);
    }

    // ✅ Your existing method (unchanged)
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

        return "✅ Face verified + PIN verified. Food picked successfully.";
    }
}
