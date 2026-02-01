package com.example.ecommerce.service;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.model.Payment;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    PaymentStatus checkPaymentStatus(String reference);

    String initiatePayment(BigDecimal totalAmount, String usd, String string);

    Payment findByReference(String reference);

    void processPaymentStatus(String reference);
}

