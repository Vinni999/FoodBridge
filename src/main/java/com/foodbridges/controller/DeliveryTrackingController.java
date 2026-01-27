package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodbridges.dto.TrackingLatestResponse;
import com.foodbridges.entity.DeliveryTracking;
import com.foodbridges.entity.Food;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.service.DeliveryTrackingService;

@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*")
public class DeliveryTrackingController {

    private final DeliveryTrackingService service;
    private final FoodRepository foodRepository;

    public DeliveryTrackingController(DeliveryTrackingService service, FoodRepository foodRepository) {
        this.service = service;
        this.foodRepository = foodRepository;
    }

    // Volunteer sends location
    @PostMapping("/update")
    public ResponseEntity<DeliveryTracking> updateTracking(
            @RequestParam Long requestId,
            @RequestParam Long volunteerId,
            @RequestParam Double lat,
            @RequestParam Double lng) {

        return ResponseEntity.ok(
                service.updateLocation(requestId, volunteerId, lat, lng)
        );
    }

    // ✅ Donor views live location + volunteer + food details
    @GetMapping("/latest")
    public ResponseEntity<TrackingLatestResponse> getLatest(@RequestParam Long requestId) {

        DeliveryTracking latest = service.getLatestLocation(requestId);
        if (latest == null) {
			return ResponseEntity.ok(null);
		}

        TrackingLatestResponse res = new TrackingLatestResponse();
        res.setRequestId(requestId);
        res.setVolunteerId(latest.getVolunteerId());
        res.setLatitude(latest.getLatitude());
        res.setLongitude(latest.getLongitude());

        // ✅ Use correct timestamp
        if (latest.getTrackedAt() != null) {
            res.setTrackedAt(latest.getTrackedAt());
        }

        Food food = foodRepository.findByRequestId(requestId).orElse(null);
        if (food != null) {
            res.setFoodId(food.getId());
            res.setStatus(food.getStatus() != null ? food.getStatus().name() : null);
        }

        return ResponseEntity.ok(res);
    }

}
