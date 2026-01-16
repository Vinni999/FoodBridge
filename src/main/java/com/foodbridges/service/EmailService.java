package com.foodbridges.service;

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
}
