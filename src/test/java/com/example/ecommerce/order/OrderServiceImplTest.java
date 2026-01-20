package com.example.ecommerce.order;

import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.payment.Payment;
import com.example.ecommerce.payment.PaymentGatewayService;
import com.example.ecommerce.payment.PaymentRepository;
import com.example.ecommerce.payment.PaymentStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentGatewayService paymentGatewayService;

    private String savedProductId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
        paymentRepository.deleteAll();

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
    void testPlaceOrderAndInitiatePayment_Success() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(2);


        OrderRequest request = new OrderRequest();
        request.setItemList(List.of(itemRequest));

        String reference = orderService.placeOrderAndInitiatePayment("temp-id", request);

        assertNotNull(reference);
        assertTrue(reference.startsWith("FAKE REF"),
                "Expected reference to start with 'FAKE REF' but got: " + reference);

        assertTrue(paymentRepository.findByReference(reference).isPresent(),
                "Payment should be persisted in the database");
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
    void testFinalizeTransaction_RealDatabase_Success() {
        Order order = new Order();
        order.setStatus(Status.PENDING);
        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setReference("REF-2026");
        payment.setOrderId(savedOrder.getId());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        orderService.finalizeTransaction("REF-2026");

        Order updatedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Payment updatedPayment = paymentRepository.findByReference("REF-2026")
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        assertEquals(Status.PAID, updatedOrder.getStatus());
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        assertNotNull(updatedPayment.getTime());
    }

    @Test
    void testFinalizeTransaction_ThrowsPaymentNotFound() {
        assertThrows(PaymentNotFoundException.class, () -> {
            orderService.finalizeTransaction("NON-EXISTENT-REF");
        });
    }

    @Test
    void getOrders_ShouldReturnFilteredResults_ById() {
        Order order1 = Order.builder()
                .id("ORD-2026-AAA")
                .status(Status.PENDING)
                .userId("user_1")
                .build();
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .id("ORD-2026-BBB")
                .status(Status.PAID)
                .userId("user_2")
                .build();
        orderRepository.save(order2);

        Page<OrderResponse> result = orderService.getOrders("AAA", PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORD-2026-AAA", result.getContent().get(0).getOrderId());
    }

    @Test
    void getOrders_ShouldReturnAllWhenSearchIsBlank() {
        Order order1 = Order.builder()
                .id("ORD-2026-AAA")
                .userId("user_1")
                .build();
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .id("ORD-2026-BBB")
                .userId("user_2")
                .build();
        orderRepository.save(order2);


        Page<OrderResponse> result = orderService.getOrders("", PageRequest.of(0, 10));

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
    }

    @Test
    void getOrders_ShouldHandlePagination() {
            Order order2 = Order.builder()
                    .id("ORD-2026-BBB")
                    .userId("user_2")
                    .build();
            orderRepository.save(order2);



        Page<OrderResponse> result = orderService.getOrders(null, PageRequest.of(0, 2));


        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

}



