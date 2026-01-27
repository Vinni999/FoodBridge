package com.foodbridges.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "delivery_tracking")
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long requestId;
    private Long volunteerId;

    private Double latitude;
    private Double longitude;

    // optional: when the tracking point was recorded
    @Column(name = "tracked_at")
    private LocalDateTime trackedAt;

    // ✅ IMPORTANT: match DB column updated_at
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (trackedAt == null) {
            trackedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getTrackedAt() { return trackedAt; }
    public void setTrackedAt(LocalDateTime trackedAt) { this.trackedAt = trackedAt; }

    // ✅ ADD THESE (missing in your file)
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
