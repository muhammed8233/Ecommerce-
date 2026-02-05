package com.example.ecommerce.serviceTest;

import com.example.ecommerce.Enum.OrderedStatus;
import com.example.ecommerce.dtos.*;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.service.*;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

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
    private ProductService productService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    @Autowired
    private RestTemplate restTemplate;

    private String savedProductId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userService.deleteAll();
        productService.deleteAll();
        paymentGatewayService.deleteAll();

        RegisterRequestDto user = new RegisterRequestDto();
        user.setName("Admin user");
        user.setEmail("limanasmau@ghost.com");
        user.setPassword("password123");
        userService.saveUser(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "limanasmau@ghost.com",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);


        ProductRequestDto product = ProductRequestDto.builder()
                .productName("Bread")
                .price(new BigDecimal("2000.00"))
                .stockQuantity(20)
                .sku("BRD01")
                .build();
        ProductResponseDto savedProduct = productService.createProduct(product);
        savedProductId = savedProduct.getProductId();
    }

    @Test
    void testInitiatePayment_Success() {
        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(2);

        OrderRequestDto request = new OrderRequestDto();
        request.setItemList(List.of(itemRequest));

        OrderResponseDto savedOrder = orderService.placeOrder(request);

        String authUrl = orderService.initiatePayment(savedOrder.getOrderId());
        assertNotNull(authUrl);
        assertTrue(authUrl.contains("checkout.paystack.com"));

        List<Payment> payments = paymentGatewayService.findAll();
        assertFalse(payments.isEmpty());
        assertNotNull(payments.get(0).getReference());


    }

    @Test
    void placeOrder() {
        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(5);

        OrderRequestDto request = new OrderRequestDto();
        request.setItemList(List.of(itemRequest));


        OrderResponseDto response = orderService.placeOrder(request);

        assertNotNull(response.getOrderId());
        assertEquals("Bread", response.getItems().getFirst().getProductName());
        assertEquals(5, response.getItems().getFirst().getQuantity());
        assertEquals(new BigDecimal("10000.00"), response.getTotalAmount());
        assertEquals(OrderedStatus.PENDING, response.getOrderedStatus());

        Product updatedProduct = productService.findById(savedProductId);
        assertEquals(15, updatedProduct.getStockQuantity());
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(25);

        OrderRequestDto request = new OrderRequestDto();
        request.setItemList(List.of(itemRequest));

        assertThrows(InsufficientStockException.class, () -> {
            orderService.placeOrder(request);
        });
    }

    @Test
    void testMarkAsPaidWithRealReference() {
        String reference = "27zfawvdbp";

        Order order = new Order();
        order.setTotalAmount(new BigDecimal("30000.00"));
        order.setOrderedStatus(OrderedStatus.PENDING);
        order = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setReference(reference);
        payment.setOrderId(order.getId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentGatewayService.savePayment(payment);

        paymentGatewayService.processPaymentStatus(reference);

        Order updatedOrder = orderRepository.findById(order.getId()).get();
        assertEquals(OrderedStatus.PAID, updatedOrder.getOrderedStatus());
    }




    @Test
    void getOrders_ShouldReturnFilteredResults_ById() {
        Order order1 = Order.builder()
                .id("ORD-2026-AAA")
                .orderedStatus(OrderedStatus.PENDING)
                .userId("user_1")
                .build();
        orderRepository.save(order1);

        Order order2 = Order.builder()
                .id("ORD-2026-BBB")
                .orderedStatus(OrderedStatus.PAID)
                .userId("user_2")
                .build();
        orderRepository.save(order2);
        System.out.println(orderRepository.findAll());
        Page<OrderResponseDto> result = orderService.getOrders("AAA", PageRequest.of(0, 10));

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

        assertEquals(2, orderRepository.findAll().size());
        Page<OrderResponseDto> result = orderService.getOrders("", PageRequest.of(0, 10));

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
        Page<OrderResponseDto> result = orderService.getOrders(null, PageRequest.of(0, 2));

        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
    }

}



