package com.example.ecommerce.service;

public interface InventoryMovementService {
    void restockProduct(String productId, int quantity);

    void deductStock(String productId, int quantity);
}
