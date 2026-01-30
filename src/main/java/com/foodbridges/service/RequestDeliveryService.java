package com.foodbridges.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodbridges.dto.CreateRequestDto;
import com.foodbridges.dto.RequestStatusDto;
import com.foodbridges.entity.Delivery;
import com.foodbridges.entity.DeliveryStatus;
import com.foodbridges.entity.Food;
import com.foodbridges.entity.FoodRequest;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.entity.RequestStatus;
import com.foodbridges.entity.User;
import com.foodbridges.repository.DeliveryRepository;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.repository.FoodRequestRepository;
import com.foodbridges.repository.UserRepository;

@Service
public class RequestDeliveryService {

    private final FoodRepository foodRepository;
    private final FoodRequestRepository requestRepository;
    private final DeliveryRepository deliveryRepository;

    private final EmailService emailService;
    private final UserRepository userRepository;

    public RequestDeliveryService(FoodRepository foodRepository,
                                  FoodRequestRepository requestRepository,
                                  DeliveryRepository deliveryRepository,
                                  EmailService emailService,
                                  UserRepository userRepository) {
        this.foodRepository = foodRepository;
        this.requestRepository = requestRepository;
        this.deliveryRepository = deliveryRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    /* =========================================================
       ✅ GET STATUS for Tracking Page (Request ID -> Status)
       ========================================================= */
    @Transactional(readOnly = true)
    public RequestStatusDto getRequestStatus(Long requestId) {

        FoodRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        String requestStatus = (req.getStatus() != null) ? req.getStatus().name() : "UNKNOWN";

        String foodStatus = "UNKNOWN";
        if (req.getFoodId() != null) {
            Food food = foodRepository.findById(req.getFoodId()).orElse(null);
            if (food != null && food.getStatus() != null) {
                foodStatus = food.getStatus().name();
            }
        }

        LocalDateTime updatedAt = LocalDateTime.now();
        String finalStatus = "REQUEST=" + requestStatus + " | FOOD=" + foodStatus;

        return new RequestStatusDto(finalStatus, updatedAt);
    }

    /* =========================================================
       ✅ CREATE REQUEST (Receiver requests food)
       ========================================================= */
    @Transactional
    public Long createRequest(CreateRequestDto dto) {

        Food food = foodRepository.findById(dto.getFoodId())
                .orElseThrow(() -> new RuntimeException("Food not found: " + dto.getFoodId()));

        if (food.getStatus() != FoodStatus.AVAILABLE) {
            throw new RuntimeException("Food is not AVAILABLE. Current status: " + food.getStatus());
        }

        // Move food to REQUESTED
        food.setStatus(FoodStatus.REQUESTED);
        foodRepository.save(food);

        FoodRequest req = new FoodRequest();
        req.setFoodId(dto.getFoodId());
        req.setReceiverId(dto.getReceiverId());
        req.setLatitude(dto.getLatitude());
        req.setLongitude(dto.getLongitude());
        req.setStatus(RequestStatus.REQUESTED);

        FoodRequest saved = requestRepository.save(req);

        // ✅ EMAIL: REQUESTED (send to donor)
        sendStatusMailToDonorOnly("REQUESTED", saved, food);

        return saved.getId();
    }

    /* =========================================================
       ✅ APPROVE BY REQUEST ID
       ========================================================= */
    @Transactional
    public FoodRequest approveRequest(Long requestId) {

        FoodRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        if (req.getFoodId() == null) {
            throw new RuntimeException("Request has no foodId, cannot approve.");
        }

        Food food = foodRepository.findById(req.getFoodId())
                .orElseThrow(() -> new RuntimeException("Food not found for requestId: " + requestId));

        if (food.getStatus() != FoodStatus.REQUESTED) {
            throw new RuntimeException("Food must be REQUESTED to accept. Current: " + food.getStatus());
        }

        // Update request status
        req.setStatus(RequestStatus.APPROVED);
        requestRepository.save(req);

        // Keep food as REQUESTED until pickup/deliver
        food.setStatus(FoodStatus.REQUESTED);
        foodRepository.save(food);

        // ✅ EMAIL: APPROVED (send to receiver + donor)
        sendStatusMailToReceiverAndDonor("APPROVED", req, food, null);

        return req;
    }

    /* =========================================================
       ✅ VOLUNTEER ACCEPT / ASSIGN (foodId + volunteerId)
       ========================================================= */
    @Transactional
    public void accept(Long foodId, Long volunteerId) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        if (food.getStatus() != FoodStatus.REQUESTED) {
            throw new RuntimeException("Food must be REQUESTED to accept. Current: " + food.getStatus());
        }

        FoodRequest req = requestRepository.findTopByFoodIdOrderByIdDesc(foodId)
                .orElseThrow(() -> new RuntimeException("Request not found for Food: " + foodId));

        // Approve request (if not already)
        req.setStatus(RequestStatus.APPROVED);
        requestRepository.save(req);

        // ✅ Save volunteer assignment in FOOD also
        food.setAssignedVolunteerId(volunteerId);
        foodRepository.save(food);

        // ✅ Create or update Delivery assignment
        Delivery delivery = deliveryRepository.findTopByFoodIdOrderByIdDesc(foodId).orElse(new Delivery());
        delivery.setFoodId(foodId);
        delivery.setRequestId(req.getId());
        delivery.setVolunteerId(volunteerId);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        delivery.setAssignedAt(LocalDateTime.now());
        deliveryRepository.save(delivery);

        // ✅ EMAIL: ASSIGNED (includes volunteer details)
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found: " + volunteerId));

        sendStatusMailToReceiverAndDonor("ASSIGNED", req, food, volunteer);
    }

    /* =========================================================
       ✅ REJECT BY REQUEST ID (needed for controller: /api/requests/{id}/reject)
       ========================================================= */
    @Transactional
    public FoodRequest rejectRequest(Long requestId) {

        FoodRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        if (req.getFoodId() == null) {
            throw new RuntimeException("Request has no foodId, cannot reject.");
        }

        Food food = foodRepository.findById(req.getFoodId())
                .orElseThrow(() -> new RuntimeException("Food not found for requestId: " + requestId));

        // Only reject if currently requested
        if (food.getStatus() != FoodStatus.REQUESTED) {
            throw new RuntimeException("Food must be REQUESTED to reject. Current: " + food.getStatus());
        }

        req.setStatus(RequestStatus.REJECTED);
        requestRepository.save(req);

        food.setStatus(FoodStatus.AVAILABLE);
        food.setAssignedVolunteerId(null);
        foodRepository.save(food);

        // Optional: update delivery record if exists (no REJECT status in enum)
        deliveryRepository.findTopByFoodIdOrderByIdDesc(food.getId()).ifPresent(delivery -> {
            deliveryRepository.save(delivery);
        });

        // Email
        sendStatusMailToReceiverAndDonor("REJECTED", req, food, null);

        return req;
    }

    /* =========================================================
       ✅ REJECT BY FOOD ID (kept for old/demo flow)
       ========================================================= */
    @Transactional
    public void reject(Long foodId) {

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        FoodRequest req = requestRepository.findTopByFoodIdOrderByIdDesc(foodId)
                .orElseThrow(() -> new RuntimeException("Request not found for Food: " + foodId));

        if (food.getStatus() != FoodStatus.REQUESTED) {
            throw new RuntimeException("Food must be REQUESTED to reject. Current: " + food.getStatus());
        }

        req.setStatus(RequestStatus.REJECTED);
        requestRepository.save(req);

        food.setStatus(FoodStatus.AVAILABLE);
        food.setAssignedVolunteerId(null);
        foodRepository.save(food);

        deliveryRepository.findTopByFoodIdOrderByIdDesc(foodId).ifPresent(delivery -> {
            deliveryRepository.save(delivery);
        });

        sendStatusMailToReceiverAndDonor("REJECTED", req, food, null);
    }

    /* =========================================================
       ✅ UPDATE FOOD STATUS (PICKED / DELIVERED)
       ========================================================= */
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

        // Face check only when DELIVERED
        if (newStatus == FoodStatus.DELIVERED) {
            Delivery delivery = deliveryRepository.findTopByFoodIdOrderByIdDesc(foodId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found for foodId: " + foodId));

            if (!delivery.isFaceVerified()) {
                throw new RuntimeException("Face verification required before completing delivery.");
            }
        }

        food.setStatus(newStatus);
        foodRepository.save(food);

        // Update delivery record timestamps/status
        deliveryRepository.findTopByFoodIdOrderByIdDesc(foodId).ifPresent(delivery -> {
        	if (newStatus == FoodStatus.PICKED) {
        	    delivery.setStatus(DeliveryStatus.PICKED_UP);
        	    delivery.setPickedUpAt(LocalDateTime.now());
        	    deliveryRepository.save(delivery);
        	}

            if (newStatus == FoodStatus.DELIVERED) {
                delivery.setStatus(DeliveryStatus.DELIVERED);
                delivery.setDeliveredAt(LocalDateTime.now());
                deliveryRepository.save(delivery);
            }
        });

        // Update request status when delivered + send emails
        requestRepository.findTopByFoodIdOrderByIdDesc(foodId).ifPresent(req -> {

            if (newStatus == FoodStatus.PICKED) {
                User volunteer = getVolunteerIfAssigned(foodId);
                sendStatusMailToReceiverAndDonor("PICKED", req, food, volunteer);
            }

            if (newStatus == FoodStatus.DELIVERED) {
                req.setStatus(RequestStatus.COMPLETED);
                requestRepository.save(req);

                User volunteer = getVolunteerIfAssigned(foodId);
                sendStatusMailToReceiverAndDonor("DELIVERED", req, food, volunteer);
            }
        });
    }

    /* =========================================================
       ✅ Helper: Volunteer lookup from Food or Delivery
       ========================================================= */
    private User getVolunteerIfAssigned(Long foodId) {

        Food food = foodRepository.findById(foodId).orElse(null);
        if (food != null && food.getAssignedVolunteerId() != null) {
            return userRepository.findById(food.getAssignedVolunteerId()).orElse(null);
        }

        return deliveryRepository.findTopByFoodIdOrderByIdDesc(foodId)
                .map(delivery -> {
                    Long vid = delivery.getVolunteerId();
                    if (vid == null) return null;
                    return userRepository.findById(vid).orElse(null);
                })
                .orElse(null);
    }

    /* =========================================================
       ✅ Email Helpers
       ========================================================= */

    private void sendStatusMailToDonorOnly(String status, FoodRequest req, Food food) {

        User donor = userRepository.findById(food.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found: " + food.getDonorId()));

        try {
            emailService.sendRequestStatusEmail(
                    donor.getEmail(),
                    status,
                    req.getId(),
                    food.getId(),
                    food.getFoodName(),
                    food.getQuantity(),
                    food.getPickupLocation(),
                    null,                // receiverName
                    donor.getName(),     // donorName
                    null,                // volunteerName
                    null                 // volunteerPhone
            );
        } catch (Exception e) {
            // ✅ Never fail API because of email limits
            System.out.println("⚠️ Donor email failed (ignored): " + e.getMessage());
        }
    }


    private void sendStatusMailToReceiverAndDonor(String status, FoodRequest req, Food food, User volunteer) {

        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + req.getReceiverId()));

        User donor = userRepository.findById(food.getDonorId())
                .orElseThrow(() -> new RuntimeException("Donor not found: " + food.getDonorId()));

        String volunteerName = (volunteer != null) ? volunteer.getName() : null;
        String volunteerPhone = (volunteer != null) ? volunteer.getPhone() : null;

        // ✅ 1) Receiver email (do not fail API if email fails)
        try {
            emailService.sendRequestStatusEmail(
                    receiver.getEmail(),
                    status,
                    req.getId(),
                    food.getId(),
                    food.getFoodName(),
                    food.getQuantity(),
                    food.getPickupLocation(),
                    receiver.getName(),
                    donor.getName(),
                    volunteerName,
                    volunteerPhone
            );
        } catch (Exception e) {
            System.out.println("⚠️ Receiver email failed (ignored): " + e.getMessage());
        }

        // ✅ Small delay helps Mailtrap "too many emails per second" issue
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        // ✅ 2) Donor email (do not fail API if email fails)
        try {
            emailService.sendRequestStatusEmail(
                    donor.getEmail(),
                    status,
                    req.getId(),
                    food.getId(),
                    food.getFoodName(),
                    food.getQuantity(),
                    food.getPickupLocation(),
                    receiver.getName(),
                    donor.getName(),
                    volunteerName,
                    volunteerPhone
            );
        } catch (Exception e) {
            System.out.println("⚠️ Donor email failed (ignored): " + e.getMessage());
        }
    }

}
