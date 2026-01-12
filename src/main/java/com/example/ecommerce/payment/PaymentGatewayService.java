package com.example.ecommerce.payment;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    PaymentStatus checkPaymentStatus(String reference);

    String initiatePayment(BigDecimal totalAmount, String usd, String string);
}

