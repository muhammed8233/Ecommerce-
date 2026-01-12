package com.example.ecommerce.inventory;

import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.product.Product;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryMovementServiceImpl implements InventoryMovementService {
    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void restockProduct(String productId, int quantity) {

        Query query = new Query(Criteria.where("_id").is(productId));
        Update update = new Update().inc("stockQuantity", quantity);

        Product updatedProduct = mongoTemplate.findAndModify(
                query,
                update,
                new FindAndModifyOptions().returnNew(true),
                Product.class
        );

        if (updatedProduct == null) {
            throw new ProductNotFoundException("Product not found: " + productId);
        }

        InventoryMovement movement = InventoryMovement.builder()
                .product(updatedProduct)
                .quantityChange(quantity)
                .reason(Reason.RESTOCK)
                .build();

        inventoryMovementRepository.save(movement);
    }

    @Override
    public void deductStock(String productId, int quantity) {

        Query query = new Query(Criteria.where("_id").is(productId)
                .and("stock").gte(quantity)
        );

        Update update = new Update().inc("stock", -quantity);

        UpdateResult result = mongoTemplate.updateFirst(query, update, Product.class);

        if (result.getModifiedCount() == 0) {
            throw new InsufficientStockException("Not enough stock for product: " + productId);
        }
    }
}
