package com.example.ecommerce.controller;

import com.example.ecommerce.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PayStackController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/paystack-webhook")
    public ResponseEntity<Void> handlePaystackWebhook(@RequestBody String payload) {
       paymentGatewayService.handlePaystackWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
