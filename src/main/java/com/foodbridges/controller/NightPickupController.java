package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foodbridges.service.NightPickupService;

@RestController
@RequestMapping("/night-pickup")
public class NightPickupController {

    private final NightPickupService nightPickupService;

    public NightPickupController(NightPickupService nightPickupService) {
        this.nightPickupService = nightPickupService;
    }

    @PostMapping("/verify-pin")
    public ResponseEntity<String> verifyPin(@RequestParam Long foodId,
                                            @RequestParam String pin) {
        return ResponseEntity.ok(nightPickupService.verifyPickupPin(foodId, pin));
    }
}
