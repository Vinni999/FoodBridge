package com.foodbridges.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "foods")
public class Food {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "food_name")
	private String foodName;

	@Column(name = "quantity")
	private int quantity;

	@Column(name = "pickup_location")
	private String pickupLocation;

	@Column(name = "expiry_time")
	private LocalDateTime expiryTime;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "donor_id")
	private Long donorId;

	@Column(name = "night_pickup")
	private boolean nightPickup;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private FoodStatus status;

	@Column(name="latitude")
	private Double latitude;

	@Column(name="longitude")
	private Double longitude;

	private Long requestId;
	private Long assignedVolunteerId;

	public Long getRequestId() { return requestId; }
	public void setRequestId(Long requestId) { this.requestId = requestId; }

	public Long getAssignedVolunteerId() { return assignedVolunteerId; }
	public void setAssignedVolunteerId(Long assignedVolunteerId) { this.assignedVolunteerId = assignedVolunteerId; }


	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (status == null) {
			status = FoodStatus.AVAILABLE;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFoodName() {
		return foodName;
	}

	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getPickupLocation() {
		return pickupLocation;
	}

	public void setPickupLocation(String pickupLocation) {
		this.pickupLocation = pickupLocation;
	}

	public LocalDateTime getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(LocalDateTime expiryTime) {
		this.expiryTime = expiryTime;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getDonorId() {
		return donorId;
	}

	public void setDonorId(Long donorId) {
		this.donorId = donorId;
	}

	public boolean isNightPickup() {
		return nightPickup;
	}

	public void setNightPickup(boolean nightPickup) {
		this.nightPickup = nightPickup;
	}

	public FoodStatus getStatus() {
		return status;
	}

	public void setStatus(FoodStatus status) {
		this.status = status;
	}
	public Double getLatitude() {
	    return latitude;
	}

	public void setLatitude(Double latitude) {
	    this.latitude = latitude;
	}

	public Double getLongitude() {
	    return longitude;
	}

	public void setLongitude(Double longitude) {
	    this.longitude = longitude;

	}


}
