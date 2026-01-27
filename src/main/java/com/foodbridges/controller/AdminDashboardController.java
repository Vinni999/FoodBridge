package com.foodbridges.controller;

import com.foodbridges.dto.AdminDashboardDto;
import com.foodbridges.entity.NightPickup;
import com.foodbridges.repository.NightPickupRepository;
import com.foodbridges.service.AdminDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final NightPickupRepository nightPickupRepository;

    public AdminDashboardController(AdminDashboardService adminDashboardService,
                                    NightPickupRepository nightPickupRepository) {
        this.adminDashboardService = adminDashboardService;
        this.nightPickupRepository = nightPickupRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDto> dashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }

    // ✅ REAL face+pin verification status from night_pickups table
    @GetMapping("/night-pickup-verified")
    public ResponseEntity<Map<Long, Boolean>> nightPickupVerified() {
        Map<Long, Boolean> map = new HashMap<>();
        for (NightPickup np : nightPickupRepository.findAll()) {
            map.put(np.getFoodId(), np.isVerified());
        }
        return ResponseEntity.ok(map);
    }
}
