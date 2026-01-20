package com.example.ecommerce.order;

public interface OrderService {

    String placeOrderAndInitiatePayment(String orderId, OrderRequest request);

    OrderResponse placeOrder(OrderRequest request);

    void finalizeTransaction(String reference);
}
