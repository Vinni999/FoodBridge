package com.foodbridges.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodbridges.dto.CreateRequestDto;
import com.foodbridges.dto.RequestStatusDto;
import com.foodbridges.entity.Delivery;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodRequest;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.entity.RequestStatus;
import com.foodbridges.repository.DeliveryRepository;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.repository.FoodRequestRepository;

@Service
public class RequestDeliveryService {

    private final FoodRepository foodRepository;
    private final FoodRequestRepository requestRepository;
    private final DeliveryRepository deliveryRepository;

    public RequestDeliveryService(FoodRepository foodRepository,
                                  FoodRequestRepository requestRepository,
                                  DeliveryRepository deliveryRepository) {
        this.foodRepository = foodRepository;
        this.requestRepository = requestRepository;
        this.deliveryRepository = deliveryRepository;
    }

    /* =========================================================
       ✅ NEW: GET STATUS for Tracking Page (Request ID -> Status)
       Endpoint will call: requestService.getRequestStatus(requestId)
       ========================================================= */
    @Transactional(readOnly = true)
    public RequestStatusDto getRequestStatus(Long requestId) {

        FoodRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        // 1) Request status (REQUESTED / APPROVED / REJECTED / COMPLETED)
        String requestStatus = (req.getStatus() != null) ? req.getStatus().name() : "UNKNOWN";

        // 2) Food status (AVAILABLE / REQUESTED / PICKED / DELIVERED / EXPIRED)
        String foodStatus = "UNKNOWN";
        if (req.getFoodId() != null) {
            Food food = foodRepository.findById(req.getFoodId()).orElse(null);
            if (food != null && food.getStatus() != null) {
                foodStatus = food.getStatus().name();
            }
        }

        // you don't have updatedAt in entity => use current time
        LocalDateTime updatedAt = LocalDateTime.now();

        // ✅ For UI, it's better to show foodStatus as main delivery status
        // but we return both inside one string OR you can extend DTO later.
        String finalStatus = "REQUEST=" + requestStatus + " | FOOD=" + foodStatus;

        return new RequestStatusDto(finalStatus, updatedAt);
    }

    @Transactional
    public Long createRequest(CreateRequestDto dto) {

        Food food = foodRepository.findById(dto.getFoodId())
                .orElseThrow(() -> new RuntimeException("Food not found: " + dto.getFoodId()));

        if (food.getStatus() != FoodStatus.AVAILABLE) {
            throw new RuntimeException("Food is not AVAILABLE. Current status: " + food.getStatus());
        }

        food.setStatus(FoodStatus.REQUESTED);
        foodRepository.save(food);

        FoodRequest req = new FoodRequest();
        req.setFoodId(dto.getFoodId());
        req.setReceiverId(dto.getReceiverId());
        req.setLatitude(dto.getLatitude());
        req.setLongitude(dto.getLongitude());
        req.setStatus(RequestStatus.REQUESTED);

        FoodRequest saved = requestRepository.save(req);
        return saved.getId();
    }

    @Transactional
    public void accept(Long foodId, Long volunteerId) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        if (food.getStatus() != FoodStatus.REQUESTED) {
            throw new RuntimeException("Food must be REQUESTED to accept. Current: " + food.getStatus());
        }

        FoodRequest req = requestRepository.findTopByFoodIdOrderByIdDesc(foodId)
                .orElseThrow(() -> new RuntimeException("Request not found for Food: " + foodId));

        req.setStatus(RequestStatus.APPROVED);
        requestRepository.save(req);

        // (Optional) You can keep food as REQUESTED until PICKED
        // food.setStatus(FoodStatus.REQUESTED);
        foodRepository.save(food);
    }

    @Transactional
    public void reject(Long foodId) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        FoodRequest req = requestRepository.findTopByFoodIdOrderByIdDesc(foodId)
                .orElseThrow(() -> new RuntimeException("Request not found for Food: " + foodId));

        req.setStatus(RequestStatus.REJECTED);
        requestRepository.save(req);

        food.setStatus(FoodStatus.AVAILABLE);
        foodRepository.save(food);
    }

    // ✅ Update Food status from UI (PICKED / DELIVERED)
    @Transactional
    public void updateFoodStatus(Long foodId, String status) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        FoodStatus newStatus;
        try {
            newStatus = FoodStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid FoodStatus: " + status);
        }

        // 🔐 FACE CHECK — only when DELIVERED
        if (newStatus == FoodStatus.DELIVERED) {

            Delivery delivery = deliveryRepository.findTopByFoodIdOrderByIdDesc(foodId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found for foodId: " + foodId));

            if (!delivery.isFaceVerified()) {
                throw new RuntimeException("Face verification required before completing delivery.");
            }
        }

        food.setStatus(newStatus);
        foodRepository.save(food);

        requestRepository.findTopByFoodIdOrderByIdDesc(foodId).ifPresent(req -> {
            if (newStatus == FoodStatus.DELIVERED) {
                req.setStatus(RequestStatus.COMPLETED);
                requestRepository.save(req);
            }
        });
    }
}
