package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodbridges.dto.CreateRequestDto;
import com.foodbridges.service.RequestDeliveryService;

@RestController
@RequestMapping("/api/request")
@CrossOrigin(origins = "*")
public class FoodRequestController {

    private final RequestDeliveryService service;

    public FoodRequestController(RequestDeliveryService service) {
        this.service = service;
    }

    // ✅ POST /api/request/create   (Body JSON)
    @PostMapping("/create")
    public ResponseEntity<Long> create(@RequestBody CreateRequestDto dto) {
        Long requestId = service.createRequest(dto);
        return ResponseEntity.ok(requestId);
    }

    // ✅ POST /api/request/accept?foodId=&volunteerId=
    @PostMapping("/accept")
    public ResponseEntity<String> accept(@RequestParam Long foodId,
                                         @RequestParam Long volunteerId) {
        service.accept(foodId, volunteerId);
        return ResponseEntity.ok("APPROVED");
    }

    // ✅ POST /api/request/reject?foodId=
    @PostMapping("/reject")
    public ResponseEntity<String> reject(@RequestParam Long foodId) {
        service.reject(foodId);
        return ResponseEntity.ok("REJECTED");
    }

    // ✅ POST /api/request/status?foodId=&status=PICKED|DELIVERED
    @PostMapping("/status")
    public ResponseEntity<String> updateStatus(@RequestParam Long foodId,
                                               @RequestParam String status) {
        service.updateFoodStatus(foodId, status);
        return ResponseEntity.ok("UPDATED");
    }
}
