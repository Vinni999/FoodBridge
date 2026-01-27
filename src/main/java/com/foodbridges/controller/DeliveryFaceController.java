package com.foodbridges.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.foodbridges.dto.FaceVerifyResponse;
import com.foodbridges.entity.Delivery;
import com.foodbridges.repository.DeliveryRepository;
import com.foodbridges.service.FaceRecognitionService;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryFaceController {

    private final DeliveryRepository deliveryRepo;
    private final FaceRecognitionService faceService;

    public DeliveryFaceController(DeliveryRepository deliveryRepo, FaceRecognitionService faceService) {
        this.deliveryRepo = deliveryRepo;
        this.faceService = faceService;
    }

    @PostMapping("/{deliveryId}/verify-face")
    public ResponseEntity<?> verifyFaceForDelivery(@PathVariable Long deliveryId,
                                                   @RequestParam Long volunteerId,
                                                   @RequestParam("image") MultipartFile image) throws Exception {

        Delivery d = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        FaceVerifyResponse resp = faceService.verifyFace(volunteerId, image);

        if (resp.isMatch()) {
            d.setFaceVerified(true);
            deliveryRepo.save(d);
            return ResponseEntity.ok(resp);
        }
        return ResponseEntity.status(403).body(resp);
    }
}
