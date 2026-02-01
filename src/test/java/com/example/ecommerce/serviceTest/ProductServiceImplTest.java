package com.example.ecommerce.serviceTest;

import com.example.ecommerce.dtos.ProductRequestDto;
import com.example.ecommerce.dtos.ProductResponseDto;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceImplTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;

   @BeforeEach
    void setUp() {
        productRepository.deleteAll();

    }

    @Test
    void createProduct() {
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .productName("bread")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("2000"))
                .sku("BHM6")
                .stockQuantity(1)
                .build();
        assertEquals(0, productRepository.count());
        productService.createProduct(productRequestDto);
        assertEquals(1, productRepository.count());
    }

    @Test
    public void testUpdateProduct(){
        ProductRequestDto productRequestDto = ProductRequestDto.builder()
                .productName("bread")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("2000"))
                .sku("BHM9")
                .stockQuantity(1)
                .build();
        assertEquals(0, productRepository.count());
        ProductResponseDto saveProduct = productService.createProduct(productRequestDto);
        assertEquals(1, productRepository.count());

        ProductRequestDto request = ProductRequestDto.builder()
                .productName("yam")
                .category("small")
                .description("small size")
                .price(new BigDecimal("2000"))
                .sku("BHM8")
                .stockQuantity(1)
                .build();
        assertEquals(1, productRepository.count());
       ProductResponseDto result = productService.updateProduct(saveProduct.getProductId(),request);
        assertEquals(1, productRepository.count());

        assertEquals("yam", result.getProductName());
        assertEquals("BHM8", result.getSku());
        assertEquals("small", result.getCategory());

    }

    @Test
    void testGetProduct() {
        Product product1 = Product.builder()
                .productName("bread")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("2000"))
                .sku("BHM2")
                .stockQuantity(1)
                .build();
        productRepository.save(product1);

        Product product2 = Product.builder()
                .productName("mjgd")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("1000"))
                .sku("BHM5")
                .stockQuantity(2)
                .build();
        productRepository.save(product2);

        Page<ProductResponseDto> result = productService.getProducts("", PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
    }
}