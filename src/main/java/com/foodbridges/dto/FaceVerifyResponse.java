package com.foodbridges.dto;

public class FaceVerifyResponse {
    private boolean match;
    private double confidence;
    private Long predictedUserId;
    private String message;

    public FaceVerifyResponse() {}

    public FaceVerifyResponse(boolean match, double confidence, Long predictedUserId, String message) {
        this.match = match;
        this.confidence = confidence;
        this.predictedUserId = predictedUserId;
        this.message = message;
    }

    public boolean isMatch() { return match; }
    public void setMatch(boolean match) { this.match = match; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public Long getPredictedUserId() { return predictedUserId; }
    public void setPredictedUserId(Long predictedUserId) { this.predictedUserId = predictedUserId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
