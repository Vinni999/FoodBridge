package com.foodbridges.dto;

import java.time.LocalDateTime;

public class TrackingLatestResponse {
    private Long requestId;
    private Long volunteerId;
    private Long foodId;
    private String status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime trackedAt;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }

    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getTrackedAt() { return trackedAt; }
    public void setTrackedAt(LocalDateTime trackedAt) { this.trackedAt = trackedAt; }
}
