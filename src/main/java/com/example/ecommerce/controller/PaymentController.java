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
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handlePaystackWebhook(@RequestBody String payload,
                                                      @RequestHeader("x-paystack-signature") String signature) {
       paymentGatewayService.handlePaystackWebhook(payload,signature);
        return ResponseEntity.ok().build();
    }
}
