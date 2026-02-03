package com.example.ecommerce.service;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.Enum.Status;
import com.example.ecommerce.dtos.PaystackInitResponseDto;
import com.example.ecommerce.dtos.PaystackVerifyResponseDto;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    static final Logger log = LoggerFactory.getLogger(PaymentGatewayServiceImpl.class);
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
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (currentUserEmail == null || !currentUserEmail.contains("@")) {
            throw new IllegalArgumentException("A valid user email is required for payment");
        }

        System.out.println("DEBUG: Attempting Paystack init with email: [" + currentUserEmail + "]");
        System.out.println("DEBUG: Key Length: " + (paystackSecretKey != null ? paystackSecretKey.length() : "NULL"));
        System.out.println("DEBUG: Key Starts With: " + (paystackSecretKey != null && paystackSecretKey.length() > 7 ? paystackSecretKey.substring(0, 7) : "INVALID"));

        try {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", currentUserEmail);
        payload.put("amount", totalAmount.multiply(new BigDecimal(100)));
        payload.put("callback_url", "https://nonenlightened-tonsorial-august.ngrok-free.dev");
        payload.put("metadata", Map.of("order_id", orderId));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);


        PaystackInitResponseDto response = restTemplate.postForObject(
                baseUrl + "/transaction/initialize", entity, PaystackInitResponseDto.class);

        if (response == null || !response.isStatus()) {
            throw new RuntimeException("Paystack Initialization Failed: " +
                    (response != null ? response.getMessage() : "No Response"));
        }

        Payment payment = new Payment();
        payment.setReference(response.getData().getReference());
        payment.setOrderId(orderId);
        payment.setAmount(totalAmount);
        payment.setPaymentStatus(PaymentStatus.PENDING);

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
        Payment payment = findByReference(reference);

        if (response != null && "success".equals(response.getData().getStatus())) {

            if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);

                orderService.markAsPaid(payment.getOrderId());
            }
        }else if (response != null && "abandoned".equals(response.getData().getStatus())) {
            LocalDateTime minute = LocalDateTime.now().minusMinutes(30);

            if(payment.getTime().isBefore(minute)) {

                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                orderService.updateStatus(payment.getOrderId(), Status.CANCELED);

                log.warn("Payment {} timed out and was CANCELED after 30 mins", reference);
            } else {
                log.info("Payment {} is still within grace period. No action taken.", reference);
            }
        } else if (response != null && "failed".equals(response.getData().getStatus())) {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                orderService.updateStatus(payment.getOrderId(), Status.CANCELED);
                log.warn("Payment {} failed explicitly", reference);
            } else {
            log.info("Payment {} is still in progress (Status: {})", reference, response.getData().getStatus());
        }
    }

    @Override
    public void deleteAll() {
        paymentRepository.deleteAll();
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public void savepayment(Payment payment) {
        paymentRepository.save(payment);
    }
}
