package com.example.ecommerce.controller;

import com.example.ecommerce.service.PaymentGatewayService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @PostMapping("/paystack-webhook")
    public ResponseEntity<Void> handlePaystackWebhook(@RequestBody String payload) {
        try {
            JsonNode rootNode = objectMapper.readTree(payload);

            // Paystack sends the reference inside the data object
            String reference = rootNode.path("data").path("reference").asText();
            String event = rootNode.path("event").asText();

            log.info("Received Paystack event: {} for reference: {}", event, reference);

            if ("charge.success".equals(event) && !reference.isEmpty()) {
                paymentGatewayService.processPaymentStatus(reference);
            }

        } catch (Exception e) {
            log.error("Error processing Paystack webhook: {}", e.getMessage());
        }

        // Paystack needs a 200 OK to know you received it
        return ResponseEntity.ok().build();
    }
}
