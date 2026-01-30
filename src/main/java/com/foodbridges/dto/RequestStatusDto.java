package com.foodbridges.dto;

import java.time.LocalDateTime;

public class RequestStatusDto {
    private String status;        // e.g., REQUESTED/PICKED/DELIVERED
    private LocalDateTime updatedAt;

    public RequestStatusDto() {}

    public RequestStatusDto(String status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
