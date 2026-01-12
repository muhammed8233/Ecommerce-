package com.example.ecommerce.inventory;

public interface InventoryMovementService {
    void restockProduct(String productId, int quantity);

    void deductStock(String productId, int quantity);
}
