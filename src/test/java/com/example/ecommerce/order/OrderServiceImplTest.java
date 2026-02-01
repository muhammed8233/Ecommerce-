package com.example.ecommerce.order;

import com.example.ecommerce.Enum.Status;
import com.example.ecommerce.dtos.OrderItemRequestDto;
import com.example.ecommerce.dtos.OrderRequestDto;
import com.example.ecommerce.dtos.OrderResponseDto;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.service.JwtService;
import com.example.ecommerce.service.PaymentGatewayService;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.Enum.PaymentStatus;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.Enum.Role;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.UserService;
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
    private UserService userService;
    @Autowired
    private PaymentGatewayService paymentGatewayService;

    private String savedProductId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userService.deleteAll();
        productRepository.deleteAll();
        paymentGatewayService.deleteAll();

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

    @Test
    void testInitiatePayment_Success() {
        OrderItemRequestDto itemRequest = new OrderItemRequestDto();
        itemRequest.setProductId(savedProductId);
        itemRequest.setQuantity(2);

        OrderRequestDto request = new OrderRequestDto();
        request.setItemList(List.of(itemRequest));

        OrderResponseDto savedOrder = orderService.placeOrder(request);

        String reference = orderService.initiatePayment(savedOrder.getOrderId());

        assertNotNull(reference);
        Payment payment = paymentGatewayService.findByReference(reference);

        assertTrue(reference.startsWith("FAKE REF"));

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertNotNull(payment.getOrderId());

        Order order = orderRepository.findById(payment.getOrderId()).get();
        assertEquals(Status.PENDING, order.getStatus());
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
        assertEquals(Status.PENDING, response.getStatus());

        Product updatedProduct = productRepository.findById(savedProductId).get();
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
    void testMarkAsPaidToReturnSuccess() {
        Order order = new Order();
        order.setStatus(Status.PENDING);
        Order savedOrder = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setReference("REF-2026");
        payment.setOrderId(savedOrder.getId());
        payment.setStatus(PaymentStatus.PENDING);


        paymentGatewayService.processPaymentStatus("REF-2026");

        Order updatedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Payment updatedPayment = paymentRepository.findByReference("REF-2026")
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        assertEquals(Status.PAID, updatedOrder.getStatus());
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        assertNotNull(updatedPayment.getTime());
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



