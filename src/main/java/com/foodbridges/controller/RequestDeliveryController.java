package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodbridges.dto.CreateRequestDto;
import com.foodbridges.dto.RequestStatusDto;
import com.foodbridges.service.RequestDeliveryService;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@CrossOrigin(origins = "*")
public class RequestDeliveryController {

    private final RequestDeliveryService requestService;

    public RequestDeliveryController(RequestDeliveryService requestService) {
        this.requestService = requestService;
    }

    /* =========================================================
       REQUEST FLOW (Old URLs kept for compatibility)
       ========================================================= */

    // ✅ OLD: POST /api/requests?foodId=&receiverId=
    // Internally calls NEW: /api/request/create logic
    @PostMapping("/api/requests")
    public ResponseEntity<Long> requestFood(@RequestParam Long foodId,
                                            @RequestParam Long receiverId) {

        CreateRequestDto dto = new CreateRequestDto();
        dto.setFoodId(foodId);
        dto.setReceiverId(receiverId);

        Long requestId = requestService.createRequest(dto);
        return ResponseEntity.ok(requestId);
    }

    // ✅ OLD: PUT /api/requests/{id}/approve
    // NOTE: old API approves by requestId, but our new service approves by foodId.
    // So we treat {id} as FOOD ID for now (demo-friendly).
    // If you want true requestId approval, tell me and I’ll add method using repository.findById(id).
    @PutMapping("/api/requests/{id}/approve")
    public ResponseEntity<String> approve(@PathVariable Long id,
                                          @RequestParam(required = false, defaultValue = "101") Long volunteerId) {

        Long foodId = id; // for demo: id acts as foodId
        requestService.accept(foodId, volunteerId);
        return ResponseEntity.ok("APPROVED");
    }
    @GetMapping("/api/requests/{requestId}/status")
    public ResponseEntity<RequestStatusDto> getStatus(@PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.getRequestStatus(requestId));
    }

    // ✅ OLD reject (optional helper)
    @PutMapping("/api/requests/{id}/reject")
    public ResponseEntity<String> reject(@PathVariable Long id) {
        Long foodId = id; // for demo: id acts as foodId
        requestService.reject(foodId);
        return ResponseEntity.ok("REJECTED");
    }

    /* =========================================================
       DELIVERY ENDPOINTS (Your old ones)
       =========================================================
       In the new flow:
       - Pickup == FoodStatus.PICKED
       - Deliver == FoodStatus.DELIVERED  (also sets RequestStatus.COMPLETED)
       So we update Food status directly using requestService.updateFoodStatus()
       ========================================================= */

    // ✅ OLD: POST /api/deliveries/assign?requestId=&volunteerId=
    // In new flow, tracking uses requestId, but status updates use foodId.
    // For demo, we just return "ASSIGNED" (tracking handled by /api/tracking/update).
    @PostMapping("/api/deliveries/assign")
    public ResponseEntity<String> assign(@RequestParam Long requestId,
                                         @RequestParam Long volunteerId) {
        // assignment logic is already covered by /api/request/accept?foodId=&volunteerId=
        // Keep this for old Postman collections
        return ResponseEntity.ok("ASSIGNED (Use /api/request/accept for actual approval)");
    }

    // ✅ OLD: PUT /api/deliveries/{id}/pickup
    // Treat {id} as FOOD ID
    @PutMapping("/api/deliveries/{id}/pickup")
    public ResponseEntity<String> pickup(@PathVariable Long id) {
        Long foodId = id;
        requestService.updateFoodStatus(foodId, "PICKED");
        return ResponseEntity.ok("PICKED");
    }

    // ✅ OLD: PUT /api/deliveries/{id}/deliver
    // Treat {id} as FOOD ID
    @PutMapping("/api/deliveries/{id}/deliver")
    public ResponseEntity<String> deliver(@PathVariable Long id) {
        Long foodId = id;
        requestService.updateFoodStatus(foodId, "DELIVERED");
        return ResponseEntity.ok("DELIVERED");
    }
}
