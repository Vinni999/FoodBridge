package com.foodbridges.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class FaceStorageService {

    private final Path facesRoot = Paths.get("uploads", "faces");
    private final Path modelRoot = Paths.get("uploads", "model");

    public FaceStorageService() throws Exception {
        Files.createDirectories(facesRoot);
        Files.createDirectories(modelRoot);
    }

    public Path userDir(Long userId) throws Exception {
        Path dir = facesRoot.resolve(String.valueOf(userId));
        Files.createDirectories(dir);
        return dir;
    }

    public Path newFaceSamplePath(Long userId) throws Exception {
        String filename = "face_" + Instant.now().toEpochMilli() + ".png";
        return userDir(userId).resolve(filename);
    }

    public Path modelPath() {
        return modelRoot.resolve("lbph-model.xml");
    }

    public Path labelsPath() {
        return modelRoot.resolve("labels.json");
    }
}
