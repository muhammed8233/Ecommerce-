package com.example.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService{
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(token + " is your verification code");

        String htmlContent = "<div style='font-family: Helvetica, Arial, sans-serif; text-align: center; padding: 40px; background-color: #f9f9f9;'>" +
                "<div style='background-color: white; padding: 20px; border-radius: 10px; display: inline-block; border: 1px solid #ddd;'>" +
                "<h2>Account Verification</h2>" +
                "<p>Please enter the following 4-digit code in the app:</p>" +
                "<div style='font-size: 48px; font-weight: bold; letter-spacing: 10px; color: #2c3e50; margin: 20px 0;'>" +
                token + "</div>" +
                "<p style='color: #7f8c8d;'>This code expires in 15 minutes.</p>" +
                "</div>" +
                "</div>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

}
