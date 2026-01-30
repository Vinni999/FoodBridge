package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foodbridges.dto.CreateRequestDto;
import com.foodbridges.dto.RequestStatusDto;
import com.foodbridges.entity.FoodRequest;
import com.foodbridges.service.RequestDeliveryService;

@RestController
@CrossOrigin(origins = "*")
public class RequestDeliveryController {

    private final RequestDeliveryService requestService;

    public RequestDeliveryController(RequestDeliveryService requestService) {
        this.requestService = requestService;
    }

    /* =========================================================
       REQUEST FLOW
       ========================================================= */

    // ✅ Create request (kept as your old compatible URL)
    // POST /api/requests?foodId=28&receiverId=78
    @PostMapping("/api/requests")
    public ResponseEntity<CreateRequestResponse> requestFood(@RequestParam Long foodId,
                                                            @RequestParam Long receiverId) {

        CreateRequestDto dto = new CreateRequestDto();
        dto.setFoodId(foodId);
        dto.setReceiverId(receiverId);

        Long requestId = requestService.createRequest(dto);
        return ResponseEntity.ok(new CreateRequestResponse(requestId));
    }

    // ✅ Small response DTO (put inside controller file at bottom OR create separate file)
    static class CreateRequestResponse {
        public Long requestId;
        public CreateRequestResponse(Long requestId) { this.requestId = requestId; }
    }


    // ✅ Approve by REQUEST ID (FIXED)
    // PUT /api/requests/4/approve
    @PutMapping("/api/requests/{id}/approve")
    public ResponseEntity<FoodRequest> approve(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.approveRequest(id));
    }

    // ✅ Reject by REQUEST ID (FIXED)
    // PUT /api/requests/4/reject
    @PutMapping("/api/requests/{id}/reject")
    public ResponseEntity<FoodRequest> rejectByRequestId(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.rejectRequest(id));
    }

    // ✅ OPTIONAL extra endpoint (reject by foodId) — kept for your old/demo flow
    // POST /reject?foodId=28
    @PostMapping("/reject")
    public ResponseEntity<String> rejectByFoodId(@RequestParam Long foodId) {
        requestService.reject(foodId);   // void reject(Long foodId) must exist
        return ResponseEntity.ok("REJECTED");
    }

    // ✅ Tracking endpoint
    // GET /api/requests/4/status
    @GetMapping("/api/requests/{requestId}/status")
    public ResponseEntity<RequestStatusDto> getStatus(@PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.getRequestStatus(requestId));
    }

    /* =========================================================
       DELIVERY ENDPOINTS
       NOTE: Here {id} is FOOD ID (as your existing flow)
       ========================================================= */

    // (Optional compatibility) Assign endpoint kept for old postman collections
    @PostMapping("/api/deliveries/assign")
    public ResponseEntity<String> assign(@RequestParam Long requestId,
                                         @RequestParam Long volunteerId) {
        return ResponseEntity.ok("ASSIGNED");
    }

    // ✅ Mark picked (treat {id} as FOOD ID)
    @PutMapping("/api/deliveries/{id}/pickup")
    public ResponseEntity<String> pickup(@PathVariable Long id) {
        requestService.updateFoodStatus(id, "PICKED");
        return ResponseEntity.ok("PICKED");
    }

    // ✅ Mark delivered (treat {id} as FOOD ID)
    @PutMapping("/api/deliveries/{id}/deliver")
    public ResponseEntity<String> deliver(@PathVariable Long id) {
        requestService.updateFoodStatus(id, "DELIVERED");
        return ResponseEntity.ok("DELIVERED");
    }
}
