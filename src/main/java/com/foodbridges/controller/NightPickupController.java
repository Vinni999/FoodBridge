package com.foodbridges.controller;

import com.foodbridges.dto.FaceVerifyResponse;
import com.foodbridges.service.FaceRecognitionService;
import com.foodbridges.service.NightPickupService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/night-pickup")
public class NightPickupController {

    private final NightPickupService nightPickupService;
    private final FaceRecognitionService faceRecognitionService;

    public NightPickupController(NightPickupService nightPickupService,
                                 FaceRecognitionService faceRecognitionService) {
        this.nightPickupService = nightPickupService;
        this.faceRecognitionService = faceRecognitionService;
    }

    // ✅ Existing PIN-only API (keep for backward compatibility)
    @PostMapping("/verify-pin")
    public ResponseEntity<String> verifyPin(@RequestParam Long foodId,
                                            @RequestParam String pin) {
        return ResponseEntity.ok(nightPickupService.verifyPickupPin(foodId, pin));
    }

    // ✅ NEW: Face + PIN verification (secure pickup)
    @PostMapping(value = "/verify-pin-face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> verifyPinWithFace(@RequestParam Long foodId,
                                               @RequestParam String pin,
                                               @RequestParam Long userId,
                                               @RequestParam("image") MultipartFile image) throws Exception {

        FaceVerifyResponse faceResp = faceRecognitionService.verifyFace(userId, image);

        if (!faceResp.isMatch()) {
            // Block pickup if face doesn't match
            return ResponseEntity.status(403).body(faceResp);
        }

        String msg = nightPickupService.verifyPickupPin(foodId, pin);
        return ResponseEntity.ok(msg);
    }
}
