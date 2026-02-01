package com.example.ecommerce.controller;

import com.example.ecommerce.dtos.OrderRequestDto;
import com.example.ecommerce.dtos.OrderResponseDto;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
   @Autowired
   private OrderService orderService;
   @Autowired
   private PaymentGatewayService paymentGatewayService;

   @PostMapping("/place")
   @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
   public ResponseEntity<OrderResponseDto> placeOrder(@RequestBody OrderRequestDto request){
       return new ResponseEntity<>(orderService.placeOrder(request), HttpStatus.CREATED);
   }

    @PostMapping("/{orderId}/initiate-payment")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> initiatePayment(@PathVariable("orderId") String orderId){
        String payment =orderService.initiatePayment(orderId);
        return ResponseEntity.ok(payment);
    }

   @PostMapping("/verify-payment")
   public ResponseEntity<String> verifyPayment(@RequestParam String reference){
       paymentGatewayService.processPaymentStatus(reference);
       return ResponseEntity.ok("verified successfully");
   }

   @GetMapping
   public ResponseEntity<Page<OrderResponseDto>> getOrders(@RequestParam String search, @PageableDefault(size = 10,sort = "orderId",
           direction = Sort.Direction.DESC)Pageable pageable){

       return ResponseEntity.ok(orderService.getOrders(search, pageable));
   }

}
