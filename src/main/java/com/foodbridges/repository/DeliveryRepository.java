package com.foodbridges.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.foodbridges.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByVolunteerId(Long volunteerId);
    List<Delivery> findByFoodId(Long foodId);
    Optional<Delivery> findTopByFoodIdOrderByIdDesc(Long foodId);
    long countByFaceVerifiedTrue();
    java.util.List<com.foodbridges.entity.Delivery> findTop10ByOrderByIdDesc();

}
