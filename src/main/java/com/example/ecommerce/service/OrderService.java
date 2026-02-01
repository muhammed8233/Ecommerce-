package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.dtos.OrderRequest;
import com.example.ecommerce.dtos.OrderResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {


    String initiatePayment(String orderId);

    OrderResponse placeOrder(OrderRequest request);

    void finalizeTransaction(String reference);

    @Nullable Page<OrderResponse> getOrders(String search, Pageable pageable);

    Order findById(String orderId);
}
