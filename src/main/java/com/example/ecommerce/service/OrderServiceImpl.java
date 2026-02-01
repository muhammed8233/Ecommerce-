package com.example.ecommerce.service;

import com.example.ecommerce.Enum.Status;
import com.example.ecommerce.dtos.OrderItemRequest;
import com.example.ecommerce.dtos.OrderRequest;
import com.example.ecommerce.dtos.OrderResponse;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryMovementService inventoryMovementService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String initiatePayment(String orderId){

            String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            Order order = findById(orderId);

            String reference = paymentGatewayService.initiatePayment(
                    order.getTotalAmount(), "USD", order.getId());



            return reference;

    }

    private Order savePendingOrder(OrderRequest request, User user, BigDecimal totalAmount) {

        Order order = Order.builder()
                .userId(user.getId())
                .status(Status.PENDING)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .orderedItems(new ArrayList<>())
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
    public @Nullable Page<OrderResponse> getOrders(String search, Pageable pageable) {
        Query query = new Query().with(pageable);

        if (search != null && !search.isBlank()) {
            String cleanSearch = search.trim();
            String regexPattern = ".*" + Pattern.quote(cleanSearch) + ".*";

            List<Criteria> orCriteria = new ArrayList<>();

            if (ObjectId.isValid(cleanSearch)) {
                orCriteria.add(Criteria.where("_id").is(new ObjectId(cleanSearch)));
            } else {
                orCriteria.add(Criteria.where("_id").regex(regexPattern, "i"));
            }

            orCriteria.add(Criteria.where("userId").regex(regexPattern, "i"));
            query.addCriteria(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
        }

        List<Order> orders = mongoTemplate.find(query, Order.class);
        Query countQuery = Query.of(query).limit(-1).skip(-1);
        long count = mongoTemplate.count(countQuery, Order.class);

        return PageableExecutionUtils.getPage(orders, pageable, () -> count)
                .map(this::mapToOrderResponse);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = (order.getOrderedItems() == null)
                ? Collections.emptyList()
                : order.getOrderedItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productName(item.getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    public Order findById(String orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found" + orderId));

    }
}
