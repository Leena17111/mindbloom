package com.mindbloom.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // RETURN boolean so controller knows if email worked
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("MindBloom – Password Reset Verification Code");

            message.setText(
                "Hello,\n\n" +
                "We received a request to reset your MindBloom password.\n\n" +
                "Your verification code is:\n\n" +
                code + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\n" +
                "MindBloom Team"
            );

            mailSender.send(message);
            return true;   // ✅ email sent
        } catch (Exception e) {
            e.printStackTrace(); // IMPORTANT
            return false;        // ❌ email failed
        }
    }
}
