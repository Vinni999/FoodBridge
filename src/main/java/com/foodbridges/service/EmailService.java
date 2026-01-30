package com.foodbridges.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender; // can be null if not configured

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailService(@Nullable JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ Base method (keep as-is)
    public void sendEmail(String to, String subject, String body) {

        // ✅ Demo-safe: if mail not configured, do not crash app
        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            System.out.println("📧 (EMAIL SKIPPED - mail not configured)");
            System.out.println("TO: " + to);
            System.out.println("SUBJECT: " + subject);
            System.out.println("BODY: " + body);
            return;
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromEmail);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        mailSender.send(msg);
        System.out.println("✅ Email sent to " + to);
    }

    /* =========================================================
       ✅ NEW: Status-based email helper (Approved/Assigned/Delivered)
       Call this from RequestDeliveryService after each status change
       ========================================================= */

    public void sendRequestStatusEmail(
            String toEmail,
            String status,              // REQUESTED / APPROVED / ASSIGNED / PICKED / DELIVERED
            Long requestId,
            Long foodId,
            String foodName,
            Integer quantity,
            String pickupLocation,
            @Nullable String receiverName,
            @Nullable String donorName,
            @Nullable String volunteerName,
            @Nullable String volunteerPhone
    ) {

        String subject = buildSubject(status, requestId);

        String body = buildBody(status, requestId, foodId, foodName, quantity, pickupLocation,
                receiverName, donorName, volunteerName, volunteerPhone);

        sendEmail(toEmail, subject, body);
    }

    private String buildSubject(String status, Long requestId) {
        String s = (status == null) ? "UPDATE" : status.toUpperCase();
        switch (s) {
            case "REQUESTED":
                return "Food Request Created 📝 (Request #" + requestId + ")";
            case "APPROVED":
                return "Food Request Approved ✅ (Request #" + requestId + ")";
            case "ASSIGNED":
                return "Volunteer Assigned 🚚 (Request #" + requestId + ")";
            case "PICKED":
                return "Food Picked Up 📦 (Request #" + requestId + ")";
            case "DELIVERED":
                return "Food Delivered ✅ (Request #" + requestId + ")";
            default:
                return "Food Request Update (Request #" + requestId + ")";
        }
    }

    private String buildBody(
            String status,
            Long requestId,
            Long foodId,
            String foodName,
            Integer quantity,
            String pickupLocation,
            String receiverName,
            String donorName,
            String volunteerName,
            String volunteerPhone
    ) {
        String s = (status == null) ? "UPDATE" : status.toUpperCase();
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        StringBuilder sb = new StringBuilder();
        sb.append("Hello,\n\n");
        sb.append("Here is an update from FoodBridge.\n\n");

        sb.append("✅ Status: ").append(s).append("\n");
        sb.append("🆔 Request ID: ").append(requestId).append("\n");
        sb.append("🍽️ Food ID: ").append(foodId).append("\n");
        if (foodName != null) sb.append("🍲 Food: ").append(foodName).append("\n");
        if (quantity != null) sb.append("📦 Quantity: ").append(quantity).append("\n");
        if (pickupLocation != null) sb.append("📍 Pickup Location: ").append(pickupLocation).append("\n");

        if (donorName != null && !donorName.isBlank()) {
            sb.append("🙋 Donor: ").append(donorName).append("\n");
        }
        if (receiverName != null && !receiverName.isBlank()) {
            sb.append("🙋 Receiver: ").append(receiverName).append("\n");
        }

        // Volunteer details only for ASSIGNED/PICKED/DELIVERED
        if ("ASSIGNED".equals(s) || "PICKED".equals(s) || "DELIVERED".equals(s)) {
            if (volunteerName != null && !volunteerName.isBlank()) {
                sb.append("\n🚚 Volunteer Details:\n");
                sb.append("Name: ").append(volunteerName).append("\n");
                sb.append("Phone: ").append(maskPhone(volunteerPhone)).append("\n");
            }
        }

        sb.append("\n🕒 Updated At: ").append(time).append("\n");
        sb.append("\nThank you,\nFoodBridge Team\n");

        return sb.toString();
    }

    private String maskPhone(String phone) {
        if (phone == null) return "N/A";
        String p = phone.trim();
        if (p.length() < 4) return "N/A";
        return "****" + p.substring(p.length() - 4);
    }
}
