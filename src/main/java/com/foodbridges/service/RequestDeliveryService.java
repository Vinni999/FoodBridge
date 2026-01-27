package com.foodbridges.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodbridges.dto.CreateRequestDto;
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
    private final DeliveryRepository deliveryRepository;   // ✅ ADD

    public RequestDeliveryService(FoodRepository foodRepository,
                                  FoodRequestRepository requestRepository,
                                  DeliveryRepository deliveryRepository) {  // ✅ ADD
        this.foodRepository = foodRepository;
        this.requestRepository = requestRepository;
        this.deliveryRepository = deliveryRepository;      // ✅ ADD
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

    // ✅ 4) Update Food status from UI (PICKED / DELIVERED)
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
