package com.example.ecommerce.inventory;

import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRequest;
import com.example.ecommerce.product.ProductResponse;
import com.example.ecommerce.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@RequiredArgsConstructor
class InventoryMovementServiceImplTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryMovementService inventoryMovementService;
    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Test
    void restockProduct() {
        ProductRequest productRequest = ProductRequest.builder()
                .productName("bread")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("2000"))
                .sku("BHM")
                .stockQuantity(1)
                .build();
        productService.createProduct(productRequest);

        Pageable pageable = PageRequest.of(0,10, Sort.by("price").ascending());
        Page<ProductResponse> productPage = productService.getProducts("bread",pageable);

        ProductResponse saved = productPage.getContent().get(0);
        assertEquals(0,inventoryMovementRepository.findAll().size());
        inventoryMovementService.restockProduct(saved.getProductId(), 20);
        assertEquals(1,inventoryMovementRepository.findAll().size());


        Product updatedProduct = productService.findProductById(saved.getProductId());
        assertEquals(21, updatedProduct.getStockQuantity());

    }
}