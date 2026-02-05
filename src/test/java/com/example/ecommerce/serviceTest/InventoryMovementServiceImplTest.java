package com.example.ecommerce.serviceTest;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.dtos.ProductRequestDto;
import com.example.ecommerce.dtos.ProductResponseDto;
import com.example.ecommerce.repository.InventoryMovementRepository;
import com.example.ecommerce.service.InventoryMovementService;
import com.example.ecommerce.service.ProductService;
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
        productService.deleteAll();
    }


    @Test
    void restockProduct() {
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .productName("rice")
                .category("white")
                .description("mudu")
                .price(new BigDecimal("1000"))
                .sku("BM21")
                .stockQuantity(10)
                .build();
        ProductResponseDto saved = productService.createProduct(productRequestDto);

        Pageable pageable = PageRequest.of(0,10, Sort.by("price").ascending());
        Page<ProductResponseDto> productPage = productService.getProducts("rice",pageable);

        assertEquals(0,inventoryMovementRepository.findAll().size());
        inventoryMovementService.restockProduct(saved.getProductId(), 30);
        assertEquals(1,inventoryMovementRepository.findAll().size());


        Product updatedProduct = productService.findProductById(saved.getProductId());
        assertEquals(40, updatedProduct.getStockQuantity());

    }
}