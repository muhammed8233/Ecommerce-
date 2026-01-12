package com.example.ecommerce.payment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByReference(String reference);
}
