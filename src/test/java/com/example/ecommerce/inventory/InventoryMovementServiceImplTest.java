package com.example.ecommerce.inventory;

import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRequest;
import com.example.ecommerce.product.ProductResponse;
import com.example.ecommerce.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setup(){
        inventoryMovementRepository.deleteAll();
    }


    @Test
    void restockProduct() {
        ProductRequest productRequest = ProductRequest.builder()
                .productName("BEANS")
                .category("white")
                .description("mudu")
                .price(new BigDecimal("2000"))
                .sku("BHM21")
                .stockQuantity(10)
                .build();
        productService.createProduct(productRequest);

        Pageable pageable = PageRequest.of(0,10, Sort.by("price").ascending());
        Page<ProductResponse> productPage = productService.getProducts("BEANS",pageable);

        ProductResponse saved = productPage.getContent().get(0);
        assertEquals(0,inventoryMovementRepository.findAll().size());
        inventoryMovementService.restockProduct(saved.getProductId(), 30);
        assertEquals(1,inventoryMovementRepository.findAll().size());


        Product updatedProduct = productService.findProductById(saved.getProductId());
        assertEquals(40, updatedProduct.getStockQuantity());

    }
}