package com.foodbridges.controller;

import com.foodbridges.dto.FaceVerifyResponse;
import com.foodbridges.service.FaceRecognitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/face")
public class FaceRecognitionController {

    private final FaceRecognitionService faceService;

    public FaceRecognitionController(FaceRecognitionService faceService) {
        this.faceService = faceService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@RequestParam Long userId,
                                      @RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image is required"));
        }

        try {
            String msg = faceService.registerFace(userId, image);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Face register failed", "details", ex.getMessage()));
        }
    }

    @PostMapping("/train")
    public ResponseEntity<?> train() {
        try {
            String msg = faceService.trainModel();
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Training failed", "details", ex.getMessage()));
        }
    }

    @PostMapping(value = "/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> verify(@RequestParam Long userId,
                                    @RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image is required"));
        }

        try {
            FaceVerifyResponse resp = faceService.verifyFace(userId, image);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException ex) {
            // Example: "Model not trained"
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            // Example: "No face detected", "Multiple faces detected"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Face verify failed", "details", ex.getMessage()));
        }
    }
}
