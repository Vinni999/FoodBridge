package com.foodbridges.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foodbridges.dto.FoodRequest;
import com.foodbridges.entity.Food;
import com.foodbridges.service.FoodService;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PostMapping
    public ResponseEntity<Food> createFood(@RequestBody FoodRequest request) {
        return ResponseEntity.ok(foodService.createFood(request));
    }

    @GetMapping
    public ResponseEntity<List<Food>> getAll() {
        return ResponseEntity.ok(foodService.getAllFoods());
    }
}
