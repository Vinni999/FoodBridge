package com.foodbridges.dto;

import java.time.LocalDateTime;

public class FoodRequest {

    private String foodName;
    private int quantity;
    private String pickupLocation;
    private LocalDateTime expiryTime;
    private Long donorId;
    private boolean nightPickup;

    private Double latitude;
    private Double longitude;

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public boolean isNightPickup() { return nightPickup; }
    public void setNightPickup(boolean nightPickup) { this.nightPickup = nightPickup; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
