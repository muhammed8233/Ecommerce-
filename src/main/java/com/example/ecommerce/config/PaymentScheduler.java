package com.example.ecommerce.config;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentScheduler {

    @Autowired
    private PaymentGatewayService paymentService;

    @Scheduled(fixedDelay = 900000)
    public void runPaymentCleanup() {
        List<Payment> pending = paymentService.findByPaymentStatus(PaymentStatus.PENDING);
        pending.forEach(p -> paymentService.processPaymentStatus(p.getReference()));
    }
}
