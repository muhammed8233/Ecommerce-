package com.example.ecommerce.service;

import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Async;


public interface EmailService {

    @Async
    void sendVerificationEmail(String toEmail, String token) throws MessagingException;
}

