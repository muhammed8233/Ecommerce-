package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;

public interface InventoryMovementService {
    void restockProduct(String productId, int quantity);

    boolean reReserveStock(Order order);
}
