package com.example.ecommerce.service;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.model.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentGatewayService {

    String initiatePayment(BigDecimal totalAmount, String usd, String string);

    Payment findByReference(String reference);

    void processPaymentStatus(String reference);

    void deleteAll();

    List<Payment> findAll();

    void savePayment(Payment payment);

    void handlePaystackWebhook(String payload, String headerSignature);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

}


