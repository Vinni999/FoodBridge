package com.foodbridges.service;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodbridges.dto.FaceVerifyResponse;
import com.foodbridges.util.ResourceFileUtil;

@Service
public class FaceRecognitionService {

	// LBPH: lower confidence is typically better.
	// For a college demo, this threshold works reasonably.
	private static final double MATCH_THRESHOLD = 70.0;

	private final FaceStorageService storage;
	private final ObjectMapper mapper = new ObjectMapper();

	private final File cascadeFile;
	private final CascadeClassifier faceDetector;
	    // 🔽 PUT YOUR CODE HERE (THIS IS THE CONSTRUCTOR)
	    public FaceRecognitionService(FaceStorageService storage) throws Exception {
	        this.storage = storage;

	        System.out.println("➡️ Loading Haar cascade: opencv/haarcascade_frontalface_default.xml");

	        this.cascadeFile = ResourceFileUtil.copyResourceToTempFile(
	                "opencv/haarcascade_frontalface_default.xml",
	                "haarcascade_",
	                ".xml"
	        );

	        System.out.println("✅ Cascade copied to: " + cascadeFile.getAbsolutePath());
	        System.out.println("✅ Cascade file size: " + cascadeFile.length());

	        this.faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());

	        if (faceDetector.empty()) {
	            throw new IllegalStateException(
	                "❌ CascadeClassifier loaded EMPTY. File path: " + cascadeFile.getAbsolutePath()
	            );
	        }

	        System.out.println("✅ Haar Cascade loaded successfully");
	    }
	/**
	 * Register one face sample for a user (store as processed grayscale 200x200
	 * PNG). Upload 3–5 samples for better accuracy.
	 */
	public String registerFace(Long userId, MultipartFile image) throws Exception {
		Mat processed = extractAndPreprocessFace(image);
		if (processed == null) {
			throw new IllegalArgumentException(
					"No face detected OR multiple faces detected. Use a clear single-face image.");
		}

		Path savePath = storage.newFaceSamplePath(userId);
		imwrite(savePath.toString(), processed);
		return "Face sample saved: " + savePath;
	}

	/**
	 * Train LBPH model from all stored samples under uploads/faces/{userId}/*.png
	 * Saves: - uploads/model/lbph-model.xml - uploads/model/labels.json (label ->
	 * userId)
	 */
	public String trainModel() throws Exception {

		Path facesRoot = Paths.get("uploads", "faces");
		if (!Files.exists(facesRoot)) {
			throw new IllegalStateException("Faces folder not found: " + facesRoot.toAbsolutePath());
		}

		// labelToUserId mapping
		Map<Integer, Long> labelToUserId = new HashMap<>();

		List<Path> userDirs = Files.list(facesRoot).filter(Files::isDirectory).collect(Collectors.toList());

		if (userDirs.isEmpty()) {
			throw new IllegalStateException("No users found in uploads/faces. Register faces first.");
		}

		int label = 0;
		List<Mat> images = new ArrayList<>();
		List<Integer> labels = new ArrayList<>();

		for (Path dir : userDirs) {
			Long userId = Long.parseLong(dir.getFileName().toString());
			labelToUserId.put(label, userId);

			List<Path> samples = Files.list(dir).filter(p -> p.toString().toLowerCase().endsWith(".png"))
					.collect(Collectors.toList());

			for (Path sample : samples) {
				Mat img = imread(sample.toString(), IMREAD_GRAYSCALE);
				if (img.empty()) {
					continue;
				}
				images.add(img);
				labels.add(label);
			}
			label++;
		}

		if (images.size() < 3) {
			throw new IllegalStateException("Not enough training data. Register at least 3 face samples total.");
		}

		Mat labelsMat = new Mat(labels.size(), 1, CV_32SC1);
		IntIndexer idx = labelsMat.createIndexer();
		for (int i = 0; i < labels.size(); i++) {
			idx.put(i, 0, labels.get(i));
		}

		LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
		recognizer.train(new MatVector(images.toArray(new Mat[0])), labelsMat);

		recognizer.save(storage.modelPath().toString());
		mapper.writeValue(storage.labelsPath().toFile(), labelToUserId);

		return "Model trained ✅ Samples: " + images.size() + " Users: " + userDirs.size();
	}

	/**
	 * Verify uploaded face image matches the expected userId.
	 */
	public FaceVerifyResponse verifyFace(Long expectedUserId, MultipartFile image) throws Exception {
		if (!Files.exists(storage.modelPath())) {
			return new FaceVerifyResponse(false, 9999, null, "Model not trained. Call /api/face/train first.");
		}
		if (!Files.exists(storage.labelsPath())) {
			return new FaceVerifyResponse(false, 9999, null, "Labels not found. Train again.");
		}

		Mat processed = extractAndPreprocessFace(image);
		if (processed == null) {
			return new FaceVerifyResponse(false, 9999, null, "No face detected OR multiple faces detected.");
		}

		LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
		recognizer.read(storage.modelPath().toString());

		Map<Integer, Long> labelToUserId = mapper.readValue(storage.labelsPath().toFile(),
				new TypeReference<Map<Integer, Long>>() {
				});

		int[] predictedLabel = new int[1];
		double[] confidence = new double[1];

		recognizer.predict(processed, predictedLabel, confidence);

		Long predictedUserId = labelToUserId.get(predictedLabel[0]);
		boolean match = predictedUserId != null && predictedUserId.equals(expectedUserId)
				&& confidence[0] <= MATCH_THRESHOLD;

		String msg = match ? "MATCH ✅ Face verified" : "NOT MATCH ❌ Face verification failed";

		return new FaceVerifyResponse(match, confidence[0], predictedUserId, msg);
	}

	/**
	 * Detect exactly one face, crop, grayscale, resize 200x200, equalize. Returns
	 * processed Mat or null if no face / multiple faces.
	 */
	private Mat extractAndPreprocessFace(MultipartFile file) throws Exception {
	    byte[] bytes = file.getBytes();

	    Mat buf = new Mat(1, bytes.length, CV_8U);
	    buf.data().put(bytes);

	    Mat img = imdecode(buf, IMREAD_COLOR);
	    if (img.empty()) return null;

	    Mat gray = new Mat();
	    cvtColor(img, gray, COLOR_BGR2GRAY);
	    equalizeHist(gray, gray);

	    RectVector faces = new RectVector();
	    faceDetector.detectMultiScale(
	            gray,
	            faces,
	            1.1,
	            5,
	            0,
	            new Size(80, 80),
	            new Size()
	    );

	    if (faces.size() != 1) return null;

	    Rect r = faces.get(0);

	    Mat face = new Mat(gray, new Rect(r.x(), r.y(), r.width(), r.height())).clone();

	    Mat resized = new Mat();
	    resize(face, resized, new Size(200, 200));

	    Mat equalized = new Mat();
	    equalizeHist(resized, equalized);

	    return equalized;
	}
}