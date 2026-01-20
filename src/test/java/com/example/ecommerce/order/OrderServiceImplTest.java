package com.example.ecommerce.order;

import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRepository;
import com.example.ecommerce.user.Role;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    private String savedProductId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();

        User user = new User();
        user.setEmail("limanasmau@ghost.com");
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "limanasmau@ghost.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);


        Product product = Product.builder()
                .productName("Bread")
                .price(new BigDecimal("2000.00"))
                .stockQuantity(20)
                .sku("BRD01")
                .build();
        Product savedProduct = productRepository.save(product);
        savedProductId = savedProduct.getId();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void placeOrderAndInitiatePayment() {

    }

    @Test
    void placeOrder() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(5);

        OrderRequest request = new OrderRequest();
        request.setItemList(List.of(itemRequest));


        OrderResponse response = orderService.placeOrder(request);

        assertNotNull(response.getOrderId());
        assertEquals("Bread", response.getProductName());
        assertEquals(5, response.getQuantity());
        assertEquals(new BigDecimal("10000.00"), response.getTotalAmount()); // 2000 * 5
        assertEquals(Status.PENDING, response.getStatus());

        Product updatedProduct = productRepository.findById(savedProductId).get();
        assertEquals(15, updatedProduct.getStockQuantity(), "Stock should be reduced from 20 to 15");
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(25);

        OrderRequest request = new OrderRequest();
        request.setItemList(List.of(itemRequest));

        assertThrows(InsufficientStockException.class, () -> {
            orderService.placeOrder(request);
        });
    }

    @Test
    void finalizeTransaction() {
    }

    @Test
    void getOrders(){}
}