package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.repository.InventoryMovementRepository;
import com.example.ecommerce.Enum.Reason;
import com.example.ecommerce.model.InventoryMovement;
import com.example.ecommerce.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    @Transactional
    public boolean reReserveStock(Order order) {
        List<OrderItem> items = order.getOrderedItems();

        boolean allInStock = items.stream()
                .map(item -> productService.findById(item.getProductId()))
                .allMatch(product -> {
                    int required = items.stream()
                            .filter(i -> i.getProductId().equals(product.getId()))
                            .findFirst()
                            .map(OrderItem::getQuantity)
                            .orElse(0);
                    return product.getStockQuantity() >= required;
                });

        if (!allInStock) return false;

        items.forEach(item -> {
            Product product = productService.findById(item.getProductId());

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            Product updatedProduct = productService.saveProduct(product);

            InventoryMovement saleLog = InventoryMovement.builder()
                    .product(updatedProduct)
                    .quantityChange(-item.getQuantity())
                    .reason(Reason.SALE)
                    .build();

            inventoryMovementRepository.save(saleLog);
        });

        return true;
    }



    @Override
    public void save(InventoryMovement movement) {
        inventoryMovementRepository.save(movement);
    }

    @Override
    public List<InventoryMovement> getAllMovements() {
        return inventoryMovementRepository.findAll();
    }

}
