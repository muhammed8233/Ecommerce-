package com.example.ecommerce.service;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderService orderService;


    @Override
    public PaymentStatus checkPaymentStatus(String reference) {
        return PaymentStatus.SUCCESS;
    }

    @Override
    public String initiatePayment(BigDecimal totalAmount, String usd, String string) {
        return "FAKE REF" + System.currentTimeMillis();
    }

    @Override
    public Payment findByReference(String reference){
       return  paymentRepository.findByReference(reference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment reference not found: " + reference));

    }

    @Override
    public void processPaymentStatus(String reference) {
        PaymentStatus status = checkPaymentStatus(reference);
        Payment payment = findByReference(reference);

        if (status == PaymentStatus.SUCCESS) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTime(LocalDateTime.now());
            paymentRepository.save(payment);


            orderService.markAsPaid(payment.getOrderId());
        }
    }
}
