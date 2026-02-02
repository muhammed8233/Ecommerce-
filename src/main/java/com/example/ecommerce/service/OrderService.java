package com.example.ecommerce.service;

import com.example.ecommerce.Enum.Status;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.dtos.OrderRequestDto;
import com.example.ecommerce.dtos.OrderResponseDto;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {


    String initiatePayment(String orderId);

    OrderResponseDto placeOrder(OrderRequestDto request);

     void markAsPaid(String orderId);

    @Nullable Page<OrderResponseDto> getOrders(String search, Pageable pageable);

    @Transactional
    void updateStatus(String orderId, Status newStatus);

    Order findById(String orderId);
}
