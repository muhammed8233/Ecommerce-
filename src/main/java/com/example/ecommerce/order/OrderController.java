package com.example.ecommerce.order;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = ",ap1/v1/order")
@RequiredArgsConstructor
public class OrderController {
   @Autowired
   private OrderService orderService;

   @PostMapping("/order")
   public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request){
       return new ResponseEntity<>(orderService.placeOrder(request), HttpStatus.NOT_FOUND);
   }

    @PostMapping("/{orderId}/initiate-payment")
    public ResponseEntity<String> initiatePayment(@PathVariable OrderRequest request){
        String payment =orderService.placeOrderAndInitiatePayment(request);
        return ResponseEntity.ok(payment);
    }

   @PostMapping("/verify-payment")
   public ResponseEntity<String> verifyPayment(@RequestParam String reference){
       orderService.finalizeTransaction(reference);
       return ResponseEntity.ok("verified successfully");
   }


}
