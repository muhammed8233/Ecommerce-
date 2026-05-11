package com.example.ecommerce.service;

import com.example.ecommerce.model.InventoryMovement;
import com.example.ecommerce.model.Order;

import java.util.List;

public interface InventoryMovementService {
    void restockProduct(String productId, int quantity);

    boolean reReserveStock(Order order);

    void save(InventoryMovement movement);

    List<InventoryMovement> getAllMovements();
}
