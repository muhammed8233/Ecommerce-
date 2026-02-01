package com.example.ecommerce.repository;

import com.example.ecommerce.model.InventoryMovement;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryMovementRepository extends MongoRepository<InventoryMovement, String> {
}
