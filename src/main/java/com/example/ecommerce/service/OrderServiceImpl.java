package com.example.ecommerce.service;

import com.example.ecommerce.Enum.Status;
import com.example.ecommerce.dtos.OrderItemRequestDto;
import com.example.ecommerce.dtos.OrderItemResponseDto;
import com.example.ecommerce.dtos.OrderRequestDto;
import com.example.ecommerce.dtos.OrderResponseDto;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.OrderRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.ecommerce.service.PaymentGatewayServiceImpl.log;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentGatewayService paymentGatewayService;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryMovementService inventoryMovementService;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String initiatePayment(String orderId){

        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        userService.findByEmail(email);

        Order order = findById(orderId);

        return paymentGatewayService.initiatePayment(
                    order.getTotalAmount(), "USD", order.getId());
    }

    private Order savePendingOrder(OrderRequestDto request, User user, BigDecimal totalAmount) {

        Order order = Order.builder()
                .userId(user.getId())
                .OrderStatus(Status.PENDING)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .orderedItems(new ArrayList<>())
                .build();

        request.getItemList().forEach
                (orderItemRequestDto -> {
                    Product product = productService.findById(orderItemRequestDto.getProductId());

                    inventoryMovementService.deductStock(product.getId(), orderItemRequestDto.getQuantity());

                    OrderItem orderItem = OrderItem.builder()
                            .productId(product.getId())
                            .name(product.getProductName())
                            .quantity(orderItemRequestDto.getQuantity())
                            .unitPrice(product.getPrice())
                            .build();

                    order.addOrderItem(orderItem);
                });

        return orderRepository.save(order);
    }


    @Override
    public OrderResponseDto placeOrder(OrderRequestDto request) {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User user = userService.findByEmail(email);

        validateStockAvailability(request);

        BigDecimal totalAmount = calculateTotal(request);

        Order order = savePendingOrder(request, user, totalAmount);

        return mapToOrderResponse(order);

    }

    @Override
    public void markAsPaid(String orderId) {
        Order order = findById(orderId);
        order.setOrderStatus(Status.PAID);
        orderRepository.save(order);
    }

    private BigDecimal calculateTotal(OrderRequestDto request) {
        return request.getItemList().stream()
                .map(itemRequest -> {
                    Product product = productService.findById(itemRequest.getProductId());
                    BigDecimal unitPrice = product.getPrice();
                    BigDecimal quantity = BigDecimal.valueOf(itemRequest.getQuantity());
                    return unitPrice.multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateStockAvailability(OrderRequestDto request) {
        List<String> productIds = request.getItemList().stream()
                .map(OrderItemRequestDto::getProductId)
                .toList();

        Map<String, Product> productMap = productService.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        for (OrderItemRequestDto item : request.getItemList()) {
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
    public @Nullable Page<OrderResponseDto> getOrders(String search, Pageable pageable) {
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

    private OrderResponseDto mapToOrderResponse(Order order) {
        List<OrderItemResponseDto> itemResponses = (order.getOrderedItems() == null)
                ? Collections.emptyList()
                : order.getOrderedItems().stream()
                .map(item -> OrderItemResponseDto.builder()
                        .productName(item.getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional
    @Override
    public void updateStatus(String orderId, Status newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        if (newStatus == Status.CANCELED) {
            restockInventory(order);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);

        log.info("Order {} status updated to {}", orderId, newStatus);
    }

    private void restockInventory(Order order) {
        order.getOrderedItems().forEach(item -> {
            productService.increaseStock(item.getProductId(), item.getQuantity());
        });
    }


    @Override
    public Order findById(String orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id: " + orderId + "not found"));

    }
}
