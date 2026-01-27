package com.foodbridges.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodbridges.entity.DeliveryTracking;

public interface DeliveryTrackingRepository
        extends JpaRepository<DeliveryTracking, Long> {

    Optional<DeliveryTracking> findTopByRequestIdOrderByUpdatedAtDesc(Long requestId);
}
