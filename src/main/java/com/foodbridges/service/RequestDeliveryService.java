package com.foodbridges.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.foodbridges.entity.Delivery;
import com.foodbridges.entity.DeliveryStatus;
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

	public RequestDeliveryService(FoodRepository foodRepository, FoodRequestRepository requestRepository,
			DeliveryRepository deliveryRepository) {
		this.foodRepository = foodRepository;
		this.requestRepository = requestRepository;
		this.deliveryRepository = deliveryRepository;
	}

	public FoodRequest createRequest(Long foodId, Long receiverId) {
		Food food = foodRepository.findById(foodId).orElseThrow(() -> new RuntimeException("Food not found"));

		if (food.getStatus() != FoodStatus.AVAILABLE) {
			throw new RuntimeException("Food not available");
		}

		FoodRequest req = new FoodRequest();
		req.setFoodId(foodId);
		req.setReceiverId(receiverId);
		req.setStatus(RequestStatus.REQUESTED);
		req.setRequestedAt(LocalDateTime.now());
		food.setStatus(FoodStatus.REQUESTED);
		foodRepository.save(food);

		return requestRepository.save(req);
	}

	public FoodRequest approveRequest(Long requestId) {
		FoodRequest req = requestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));

		req.setStatus(RequestStatus.APPROVED);
		return requestRepository.save(req);
	}

	public Delivery assignVolunteer(Long requestId, Long volunteerId) {
		FoodRequest req = requestRepository.findById(requestId)
				.orElseThrow(() -> new RuntimeException("Request not found"));

		if (req.getStatus() != RequestStatus.APPROVED) {
			throw new RuntimeException("Request must be APPROVED first");
		}

		Delivery d = new Delivery();
		d.setRequestId(req.getId());
		d.setFoodId(req.getFoodId());
		d.setVolunteerId(volunteerId);
		d.setStatus(DeliveryStatus.ASSIGNED);
		d.setAssignedAt(LocalDateTime.now());
		Food food = foodRepository.findById(req.getFoodId()).orElseThrow(() -> new RuntimeException("Food not found"));
		food.setStatus(FoodStatus.REQUESTED);
		foodRepository.save(food);

		return deliveryRepository.save(d);
	}

	public Delivery markPickedUp(Long deliveryId) {
		Delivery d = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new RuntimeException("Delivery not found"));

		d.setStatus(DeliveryStatus.PICKED_UP);
		d.setPickedUpAt(LocalDateTime.now());

		Food food = foodRepository.findById(d.getFoodId()).orElseThrow(() -> new RuntimeException("Food not found"));
		food.setStatus(FoodStatus.PICKED);
		foodRepository.save(food);

		return deliveryRepository.save(d);
	}

	public Delivery markDelivered(Long deliveryId) {
		Delivery d = deliveryRepository.findById(deliveryId)
				.orElseThrow(() -> new RuntimeException("Delivery not found"));

		d.setStatus(DeliveryStatus.DELIVERED);
		d.setDeliveredAt(LocalDateTime.now());

		Food food = foodRepository.findById(d.getFoodId()).orElseThrow(() -> new RuntimeException("Food not found"));
		food.setStatus(FoodStatus.DELIVERED);
		foodRepository.save(food);

		FoodRequest req = requestRepository.findById(d.getRequestId())
				.orElseThrow(() -> new RuntimeException("Request not found"));
		req.setStatus(RequestStatus.COMPLETED);
		requestRepository.save(req);

		return deliveryRepository.save(d);
	}
}
