package com.foodbridges.service;

import com.foodbridges.dto.AdminDashboardDto;
import com.foodbridges.entity.FoodStatus;
import com.foodbridges.entity.RequestStatus;
import com.foodbridges.repository.DeliveryRepository;
import com.foodbridges.repository.FoodRepository;
import com.foodbridges.repository.FoodRequestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminDashboardService {

    private final FoodRepository foodRepo;
    private final FoodRequestRepository requestRepo;
    private final DeliveryRepository deliveryRepo;

    public AdminDashboardService(FoodRepository foodRepo,
                                 FoodRequestRepository requestRepo,
                                 DeliveryRepository deliveryRepo) {
        this.foodRepo = foodRepo;
        this.requestRepo = requestRepo;
        this.deliveryRepo = deliveryRepo;
    }

    public AdminDashboardDto getDashboard() {
        AdminDashboardDto dto = new AdminDashboardDto();

        dto.totalFoods = foodRepo.count();
        dto.availableFoods = foodRepo.countByStatus(FoodStatus.AVAILABLE);
        dto.requestedFoods = foodRepo.countByStatus(FoodStatus.REQUESTED);
        dto.deliveredFoods = foodRepo.countByStatus(FoodStatus.DELIVERED);

        // If you have status EXPIRED in FoodStatus
        try {
            dto.expiredFoods = foodRepo.countByStatus(FoodStatus.valueOf("EXPIRED"));
        } catch (Exception e) {
            dto.expiredFoods = 0; // will be added in auto-expiry task
        }

        dto.activeRequests = requestRepo.countByStatus(RequestStatus.REQUESTED)
                + requestRepo.countByStatus(RequestStatus.APPROVED);

        dto.completedRequests = requestRepo.countByStatus(RequestStatus.COMPLETED);

        dto.totalDeliveries = deliveryRepo.count();
        dto.faceVerifiedDeliveries = deliveryRepo.countByFaceVerifiedTrue();

        // latest lists (top 10)
        dto.latestFoods = foodRepo.findTop10ByOrderByIdDesc();
        dto.latestRequests = requestRepo.findTop10ByOrderByIdDesc();
        dto.latestDeliveries = deliveryRepo.findTop10ByOrderByIdDesc();

        return dto;
    }
}
