package com.example.ecommerce.service;

import com.example.ecommerce.repository.InventoryMovementRepository;
import com.example.ecommerce.Enum.Reason;
import com.example.ecommerce.model.InventoryMovement;
import com.example.ecommerce.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {
    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ProductService productService;

    @Override
    public void restockProduct(String productId, int quantity) {
        Product product = productService.findById(productId);

        product.setStockQuantity(product.getStockQuantity() + quantity);

        Product updatedProduct = productService.saveProduct(product);

        InventoryMovement movement = InventoryMovement.builder()
                .product(updatedProduct)
                .quantityChange(quantity)
                .reason(Reason.RESTOCK)
                .build();

        inventoryMovementRepository.save(movement);
    }
}
