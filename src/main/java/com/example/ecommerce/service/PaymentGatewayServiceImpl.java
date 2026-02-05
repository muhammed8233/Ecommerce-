package com.example.ecommerce.service;

import com.example.ecommerce.Enum.OrderedStatus;
import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.dtos.PaystackInitResponseDto;
import com.example.ecommerce.dtos.PaystackVerifyResponseDto;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private  ObjectMapper objectMapper;

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

        try {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", currentUserEmail);
        payload.put("amount", totalAmount.multiply(new BigDecimal(100)));
        payload.put("callback_url", "https://nonenlightened-tonsorial-august.ngrok-free.dev/api/v1/orders/" + orderId);
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
        Payment payment = findByReference(reference);
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            log.info("Payment {} already marked as SUCCESS. Skipping.", reference);
            return;
        }

        try{
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

                if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
                    payment.setPaymentStatus(PaymentStatus.SUCCESS);
                    paymentRepository.save(payment);
                }

                Order order = orderService.findById(payment.getOrderId());

                if (order.getOrderedStatus() == OrderedStatus.CANCELLED || order.getOrderedStatus() != OrderedStatus.PAID) {
                    log.info("Updating Order {} status to PAID (Previous status: {})", order.getId(), order.getOrderedStatus());
                    orderService.markAsPaid(order.getId());
                }
            }else if (response != null && "abandoned".equals(response.getData().getStatus())) {

                if(payment.getTime().isBefore(LocalDateTime.now().minusMinutes(30))) {

                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    orderService.updateStatus(payment.getOrderId(), OrderedStatus.CANCELLED);

                    log.warn("Payment {} timed out and was CANCELED after 30 mins", reference);
                } else {
                    log.info("Payment {} is still within grace period. No action taken.", reference);
                }
            } else if (response != null && "failed".equals(response.getData().getStatus())) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    orderService.updateStatus(payment.getOrderId(), OrderedStatus.CANCELLED);
                    log.warn("Payment {} failed explicitly", reference);
                } else {
                log.info("Payment {} is still in progress (OrderedStatus: {})", reference, response.getData().getStatus());
            }
        } catch (HttpClientErrorException e) {
            log.error("Paystack API error for {}: {} - {}", reference, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error verifying payment {}: {}", reference, e.getMessage());
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
    public void savePayment(Payment payment) {
        paymentRepository.save(payment);
    }


    @Override
    public void handlePaystackWebhook(String payload, String headerSignature) {
        try {
            if (!isSignatureValid(payload, headerSignature, paystackSecretKey)) {
                log.error("Invalid Paystack signature! Request rejected.");
                return;
            }

            JsonNode rootNode = objectMapper.readTree(payload);
            String event = rootNode.path("event").asText();
            String reference = rootNode.path("data").path("reference").asText();

            if ("charge.success".equals(event) && !reference.isEmpty()) {
                processPaymentStatus(reference);
            }

        } catch (Exception e) {
            log.error("Error processing Paystack webhook: {}", e.getMessage());
        }
    }

    @Override
    public List<Payment> findByPaymentStatus(PaymentStatus paymentStatus) {
        return paymentRepository.findByPaymentStatus(paymentStatus);
    }

    private boolean isSignatureValid(String payload, String headerSignature, String secretKey) throws Exception {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        sha512Hmac.init(secretKeySpec);

        byte[] hash = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }

        return result.toString().equals(headerSignature);
    }
}
