package com.example.ecommerce.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService{
    @Override
    public PaymentStatus checkPaymentStatus(String reference) {
        return null;
    }

    @Override
    public String initiatePayment(BigDecimal totalAmount, String usd, String string) {
        return "";
    }
}
