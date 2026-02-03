package com.example.ecommerce.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService{
    @Autowired
    private JavaMailSender mailSender;

    @Async
    @Override
    public void sendVerificationEmail(String toEmail, String token) throws MessagingException {
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Verify your Ecommerce Account");
        helper.setText("<h3>Welcome!</h3>" +
                "<p>Please click the link below to verify your account:</p>" +
                "<a href=\"" + verificationUrl + "\">Verify Account</a>", true);

        mailSender.send(message);
    }
}
