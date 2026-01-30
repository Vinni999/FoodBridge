package com.foodbridges.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.foodbridges.dto.FaceVerifyResponse;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.entity.NightPickup;
import com.foodbridges.entity.User;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.repository.NightPickupRepository;
import com.foodbridges.repository.UserRepository;

@Service
public class NightPickupService {

    private final NightPickupRepository nightPickupRepository;
    private final FoodRepository foodRepository;
    private final EmailService emailService;
    private final FaceRecognitionService faceRecognitionService;
    private final UserRepository userRepository;

    private final SecureRandom random = new SecureRandom();

    public NightPickupService(
            NightPickupRepository nightPickupRepository,
            FoodRepository foodRepository,
            EmailService emailService,
            FaceRecognitionService faceRecognitionService,
            UserRepository userRepository) {
        this.nightPickupRepository = nightPickupRepository;
        this.foodRepository = foodRepository;
        this.emailService = emailService;
        this.faceRecognitionService = faceRecognitionService;
        this.userRepository = userRepository;
    }

    /* =========================================================
       ✅ NEW: AUTO GENERATE PIN (create/update NightPickup row)
       Call this after APPROVED/ASSIGNED (best)
       ========================================================= */
    @Transactional
    public String generatePickupPin(Long foodId) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        // Only allow PIN generation for night pickup foods (optional rule)
        if (!food.isNightPickup()) {
            throw new RuntimeException("Night pickup is not enabled for this food.");
        }

        // Expiry check
        if (food.getExpiryTime() != null && food.getExpiryTime().isBefore(LocalDateTime.now())) {
            food.setStatus(FoodStatus.EXPIRED);
            foodRepository.save(food);
            throw new RuntimeException("Food expired, cannot generate PIN.");
        }

        // Find existing pickup record or create new
        NightPickup pickup = nightPickupRepository.findByFoodId(foodId).orElse(new NightPickup());
        pickup.setFoodId(foodId);

        // Generate fresh 6-digit PIN (you can change to 4 digits if you want)
        String pin = generate6DigitPin();
        pickup.setPin(pin);

        // reset state for new pin
        pickup.setVerified(false);
        pickup.setUsed(false);
        pickup.setUsedAt(null);
        pickup.setCreatedAt(LocalDateTime.now()); // if field exists in your NightPickup entity

        nightPickupRepository.save(pickup);

        // ✅ Send email (donor + assigned volunteer if available)
        sendPinEmail(food, pin);

        return "✅ Pickup PIN generated & sent: " + pin;
    }

    private String generate6DigitPin() {
        int val = 100000 + random.nextInt(900000);
        return String.valueOf(val);
    }

    private void sendPinEmail(Food food, String pin) {

        // 1) Donor
        if (food.getDonorId() != null) {
            userRepository.findById(food.getDonorId()).ifPresent(donor -> {
                emailService.sendEmail(
                        donor.getEmail(),
                        "FoodBridge - Night Pickup PIN",
                        buildPinMessage(donor.getName(), food, pin, "DONOR")
                );
            });
        }

        // 2) Volunteer (if assigned)
        if (food.getAssignedVolunteerId() != null) {
            userRepository.findById(food.getAssignedVolunteerId()).ifPresent(volunteer -> {
                emailService.sendEmail(
                        volunteer.getEmail(),
                        "FoodBridge - Night Pickup PIN",
                        buildPinMessage(volunteer.getName(), food, pin, "VOLUNTEER")
                );
            });
        }
    }

    private String buildPinMessage(String name, Food food, String pin, String role) {
        return "Hi " + (name == null ? "" : name) + ",\n\n"
                + "Night pickup PIN generated.\n\n"
                + "Role: " + role + "\n"
                + "Food ID: " + food.getId() + "\n"
                + "Food: " + food.getFoodName() + "\n"
                + "Pickup Location: " + food.getPickupLocation() + "\n"
                + "PIN: " + pin + "\n\n"
                + "Use this PIN at pickup verification.\n"
                + "Thanks,\nFoodBridge";
    }

    /* =========================================================
       ✅ Face + PIN verification (your method, unchanged behavior)
       ========================================================= */
    @Transactional
    public Object verifyPickupPinWithFace(Long foodId, String enteredPin, Long userId, MultipartFile image) throws Exception {

        FaceVerifyResponse faceResp = faceRecognitionService.verifyFace(userId, image);

        // If your DTO uses getMatch(), change below to: if (!faceResp.getMatch())
        if (!faceResp.isMatch()) {
            return faceResp;
        }

        return verifyPickupPin(foodId, enteredPin);
    }

    /* =========================================================
       ✅ PIN verification (your existing method - kept)
       ========================================================= */
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
