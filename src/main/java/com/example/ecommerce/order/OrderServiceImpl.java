package com.example.ecommerce.order;

import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.inventory.InventoryMovementRepository;
import com.example.ecommerce.inventory.InventoryMovementService;
import com.example.ecommerce.payment.Payment;
import com.example.ecommerce.payment.PaymentGatewayService;
import com.example.ecommerce.payment.PaymentRepository;
import com.example.ecommerce.payment.PaymentStatus;
import com.example.ecommerce.product.Product;
import com.example.ecommerce.product.ProductRepository;
import com.example.ecommerce.user.User;
import com.example.ecommerce.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;
    @Autowired
    private InventoryMovementService inventoryMovementService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String placeOrderAndInitiatePayment(String orderId,  OrderRequest request){

            String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            BigDecimal totalAmount = calculateTotal(request);

            Order order = savePendingOrder(request, user, totalAmount);

            String reference = paymentGatewayService.initiatePayment(
                    order.getTotalAmount(), "USD", order.getId());

            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .reference(reference)
                    .status(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(payment);

            return reference;

    }

    private Order savePendingOrder(OrderRequest request, User user, BigDecimal totalAmount) {

        Order order = Order.builder()
                .userId(user.getId())
                .status(Status.PENDING)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        request.getItemList().forEach
                (orderItemRequest -> {
                    Product product = productRepository.findById(orderItemRequest.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + orderItemRequest.getProductId()));

                    inventoryMovementService.deductStock(product.getId(), orderItemRequest.getQuantity());

                    OrderItem orderItem = OrderItem.builder()
                            .productId(product.getId())
                            .name(product.getProductName())
                            .quantity(orderItemRequest.getQuantity())
                            .unitPrice(product.getPrice())
                            .build();

                    order.addOrderItem(orderItem);
                });

        return orderRepository.save(order);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        // 1. Check if items exist to avoid IndexOutOfBoundsException
        OrderItem firstItem = (order.getItems() != null && !order.getItems().isEmpty())
                ? order.getItems().get(0)
                : null;

        return OrderResponse.builder()
                .orderId(order.getId())
                // FIX: Map from the item, not the order
                .productName(firstItem != null ? firstItem.getName() : "Multiple Items")
                .quantity(firstItem != null ? firstItem.getQuantity() : 0)
                .unitPrice(firstItem != null ? firstItem.getUnitPrice() : null)
                // These are already working
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }



    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        validateStockAvailability(request);

        BigDecimal totalAmount = calculateTotal(request);

        Order order = savePendingOrder(request, user, totalAmount);

        return mapToOrderResponse(order);

    }

    private BigDecimal calculateTotal(OrderRequest request) {
        return request.getItemList().stream()
                .map(itemRequest -> {
                    Product product = productRepository.findById(itemRequest.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemRequest.getProductId()));

                    BigDecimal unitPrice = product.getPrice();
                    BigDecimal quantity = BigDecimal.valueOf(itemRequest.getQuantity());
                    return unitPrice.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
    private void validateStockAvailability(OrderRequest request) {
        List<String> productIds = request.getItemList().stream()
                .map(OrderItemRequest::getProductId)
                .toList();

        Map<String, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (OrderItemRequest item : request.getItemList()) {
            Product product = productMap.get(item.getProductId());

            if (product == null) {
                throw new ProductNotFoundException("Product not found: " + item.getProductId());
            }

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Product %s has insufficient stock. Available: %d, Requested: %d",
                                product.getProductName(), product.getStockQuantity(), item.getQuantity())
                );
            }
        }
    }


    @Override
    public void finalizeTransaction(String reference) {
        PaymentStatus status = paymentGatewayService.checkPaymentStatus(reference);

        if (status == PaymentStatus.SUCCESS) {
            Payment payment = paymentRepository.findByReference(reference)
                    .orElseThrow(() -> new PaymentNotFoundException("Payment reference not found: " + reference));

            Order order = orderRepository.findById(payment.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found for payment: " + reference));

            order.setStatus(Status.PAID);
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTime(LocalDateTime.now());

            orderRepository.save(order);
            paymentRepository.save(payment);
        }
    }

    @Override
    public @Nullable Page<OrderResponse> getOrders(String search, Pageable pageable) {
        Query query = new Query().with(pageable);

        if (search != null && !search.isBlank()) {
            String regexPattern = ".*" + search + ".*";

            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("_id").regex(regexPattern, "i"),
                    Criteria.where("userId").regex(regexPattern, "i")
            );

            query.addCriteria(criteria);
        }

        List<Order> orders = mongoTemplate.find(query, Order.class);
        long count = mongoTemplate.count(query.skip(-1).limit(-1), Order.class);
        return PageableExecutionUtils.getPage(orders, pageable, () -> count)
                .map(this::mapToOrderResponse);
    }

}
