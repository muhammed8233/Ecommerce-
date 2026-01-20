package com.example.ecommerce.product;

import com.example.ecommerce.order.OrderResponse;
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
        ProductRequest productRequest = ProductRequest.builder()
                .productName("bread")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("2000"))
                .sku("BHM6")
                .stockQuantity(1)
                .build();
        assertEquals(0, productRepository.findAll().size());
        productService.createProduct(productRequest);
        assertEquals(1, productRepository.findAll().size());
    }

    @Test
    public void testUpdateProduct(){
        ProductRequest productRequest = ProductRequest.builder()
                .productName("bread")
                .category("medium")
                .description("family size")
                .price(new BigDecimal("2000"))
                .sku("BHM9")
                .stockQuantity(1)
                .build();
        assertEquals(0, productRepository.findAll().size());
        ProductResponse saveProduct = productService.createProduct(productRequest);
        assertEquals(1, productRepository.findAll().size());

        ProductRequest request = ProductRequest.builder()
                .productName("yam")
                .category("small")
                .description("small size")
                .price(new BigDecimal("2000"))
                .sku("BHM8")
                .stockQuantity(1)
                .build();
        assertEquals(1, productRepository.findAll().size());
       ProductResponse result = productService.updateProduct(saveProduct.getProductId(),request);
        assertEquals(1, productRepository.findAll().size());

        assertEquals("yam", result.getProductName());
        assertEquals("BHM8", result.getSku());
        assertEquals("small", result.getCategory());

    }

    @Test
    void testGetProduct(){
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

        Page<ProductResponse> result = productService.getProducts("", PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
    }


}