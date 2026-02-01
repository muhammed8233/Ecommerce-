package com.example.ecommerce.service;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.Enum.Status;
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
    public void finalizeTransaction(String reference) {
        PaymentStatus status = checkPaymentStatus(reference);

        if (status == PaymentStatus.SUCCESS) {
            Payment payment = findByReference(reference);

            Order order = orderService.findById(payment.getOrderId());
            order.setStatus(Status.PAID);
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTime(LocalDateTime.now());

            orderRepository.save(order);
        }
    }
}
