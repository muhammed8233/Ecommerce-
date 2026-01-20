package com.example.ecommerce.order;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    String placeOrderAndInitiatePayment(String orderId, OrderRequest request);

    OrderResponse placeOrder(OrderRequest request);

    void finalizeTransaction(String reference);

    @Nullable Page<OrderResponse> getOrders(String search, Pageable pageable);

}
