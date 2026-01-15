package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foodbridges.entity.Delivery;
import com.foodbridges.entity.FoodRequest;
import com.foodbridges.service.RequestDeliveryService;

@RestController
public class RequestDeliveryController {

	private final RequestDeliveryService service;

	public RequestDeliveryController(RequestDeliveryService service) {
		this.service = service;
	}

	@PostMapping("/api/requests")
	public ResponseEntity<FoodRequest> requestFood(@RequestParam Long foodId, @RequestParam Long receiverId) {
		return ResponseEntity.ok(service.createRequest(foodId, receiverId));
	}

	@PutMapping("/api/requests/{id}/approve")
	public ResponseEntity<FoodRequest> approve(@PathVariable Long id) {
		return ResponseEntity.ok(service.approveRequest(id));
	}

	@PostMapping("/api/deliveries/assign")
	public ResponseEntity<Delivery> assign(@RequestParam Long requestId, @RequestParam Long volunteerId) {
		return ResponseEntity.ok(service.assignVolunteer(requestId, volunteerId));
	}

	@PutMapping("/api/deliveries/{id}/pickup")
	public ResponseEntity<Delivery> pickup(@PathVariable Long id) {
		return ResponseEntity.ok(service.markPickedUp(id));
	}

	@PutMapping("/api/deliveries/{id}/deliver")
	public ResponseEntity<Delivery> deliver(@PathVariable Long id) {
		return ResponseEntity.ok(service.markDelivered(id));
	}
}
