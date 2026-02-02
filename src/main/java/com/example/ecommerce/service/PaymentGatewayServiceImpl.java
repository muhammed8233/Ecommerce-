package com.example.ecommerce.service;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.dtos.PaystackInitResponse;
import com.example.ecommerce.dtos.PaystackVerifyResponseDto;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${paystack.base.url}")
    private String baseUrl;

    @Value("${paystack.secret.key}")
    private String paystackSecretKey;

    @Override
    @Transactional
    public String initiatePayment(BigDecimal totalAmount, String email, String orderId) {
        try {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("amount", totalAmount.multiply(new BigDecimal(100)));
        payload.put("callback_url", "http://your-frontend-link.com");
        payload.put("metadata", Map.of("order_id", orderId));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);


        PaystackInitResponse response = restTemplate.postForObject(
                baseUrl + "/transaction/initialize", entity, PaystackInitResponse.class);

        if (response == null || !response.isStatus()) {
            throw new RuntimeException("Paystack Initialization Failed: " +
                    (response != null ? response.getMessage() : "No Response"));
        }

        Payment payment = new Payment();
        payment.setReference(response.getData().getReference());
        payment.setOrderId(orderId);
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTime(LocalDateTime.now());

        paymentRepository.save(payment);

        return response.getData().getAuthorizationUrl();

        } catch (Exception e) {
            throw new RuntimeException("Could not initiate payment: " + e.getMessage());
        }
    }



    @Override
    public Payment findByReference(String reference){
       return  paymentRepository.findByReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment reference not found: " + reference));

    }

    @Override
    @Transactional
    public void processPaymentStatus(String reference) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecretKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<PaystackVerifyResponseDto> responseEntity = restTemplate.exchange(
                baseUrl + "/transaction/verify/" + reference,
                HttpMethod.GET,
                entity,
                PaystackVerifyResponseDto.class
        );

        PaystackVerifyResponseDto response = responseEntity.getBody();

        if (response != null && "success".equals(response.getData().getStatus())) {
            Payment payment = findByReference(reference);

            if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);

                orderService.markAsPaid(payment.getOrderId());
            }
        }
    }

    @Override
    public void deleteAll() {
        paymentRepository.deleteAll();
    }
}
