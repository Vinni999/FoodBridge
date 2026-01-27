package com.foodbridges.dto;

import java.util.List;

public class AdminDashboardDto {

    // cards
    public long totalFoods;
    public long availableFoods;
    public long requestedFoods;
    public long deliveredFoods;
    public long expiredFoods;

    public long activeRequests;
    public long completedRequests;

    public long totalDeliveries;
    public long faceVerifiedDeliveries;

    // tables (latest lists)
    public List<?> latestFoods;
    public List<?> latestRequests;
    public List<?> latestDeliveries;
}
