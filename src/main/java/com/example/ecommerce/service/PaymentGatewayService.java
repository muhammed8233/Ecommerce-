package com.example.ecommerce.service;

import com.example.ecommerce.model.Payment;

import java.math.BigDecimal;

public interface PaymentGatewayService {

    String initiatePayment(BigDecimal totalAmount, String usd, String string);

    Payment findByReference(String reference);

    void processPaymentStatus(String reference);

    void deleteAll();
}


