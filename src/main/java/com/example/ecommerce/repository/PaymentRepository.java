package com.example.ecommerce.repository;

import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByReference(String reference);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

}
