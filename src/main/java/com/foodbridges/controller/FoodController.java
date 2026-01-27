package com.foodbridges.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.foodbridges.dto.FoodRequest;
import com.foodbridges.entity.Food;
import com.foodbridges.service.FoodService;

@RestController
@RequestMapping("/api/food")
@CrossOrigin(origins = "*")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    // ✅ matches JS: POST /api/food/add
    @PostMapping("/add")
    public ResponseEntity<Food> addFood(@RequestBody FoodRequest request) {
        return ResponseEntity.ok(foodService.createFood(request));
    }

    // ✅ matches JS: GET /api/food/all
    @GetMapping("/all")
    public ResponseEntity<List<Food>> getAllFoods() {
        return ResponseEntity.ok(foodService.getAllFoods());
    }

    // ✅ matches JS: GET /api/food/search?location=
    @GetMapping("/search")
    public ResponseEntity<List<Food>> searchByLocation(@RequestParam String location) {
        return ResponseEntity.ok(foodService.searchByLocation(location));
    }

    // ✅ matches JS: GET /api/food/nearby?lat=&lng=&radiusKm=
    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyFoods(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm) {
        return ResponseEntity.ok(foodService.getNearbyFoods(lat, lng, radiusKm));
    }
}
