package com.foodbridges.service;

import org.springframework.stereotype.Service;

import com.foodbridges.entity.DeliveryTracking;
import com.foodbridges.repository.DeliveryTrackingRepository;

@Service
public class DeliveryTrackingService {

    private final DeliveryTrackingRepository repository;

    public DeliveryTrackingService(DeliveryTrackingRepository repository) {
        this.repository = repository;
    }

    public DeliveryTracking updateLocation(Long requestId,
                                           Long volunteerId,
                                           Double latitude,
                                           Double longitude) {

        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setRequestId(requestId);
        tracking.setVolunteerId(volunteerId);
        tracking.setLatitude(latitude);
        tracking.setLongitude(longitude);

        return repository.save(tracking);
    }

    public DeliveryTracking getLatestLocation(Long requestId) {
        return repository
                .findTopByRequestIdOrderByTrackedAtDesc(requestId)
                .orElse(null);
    }
}
