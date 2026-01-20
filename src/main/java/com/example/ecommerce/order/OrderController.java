package com.example.ecommerce.order;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
   @Autowired
   private OrderService orderService;

   @PostMapping
   @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
   public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request){
       return new ResponseEntity<>(orderService.placeOrder(request), HttpStatus.CREATED);
   }

    @PostMapping("/{orderId}/initiate-payment")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> initiatePayment(@PathVariable("orderId") String orderId,
                                                   @RequestBody OrderRequest request){
        String payment =orderService.placeOrderAndInitiatePayment(orderId, request);
        return ResponseEntity.ok(payment);
    }

   @PostMapping("/verify-payment")
   public ResponseEntity<String> verifyPayment(@RequestParam String reference){
       orderService.finalizeTransaction(reference);
       return ResponseEntity.ok("verified successfully");
   }


}
