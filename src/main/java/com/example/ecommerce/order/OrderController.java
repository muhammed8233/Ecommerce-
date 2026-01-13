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

   @PostMapping("/{id}/pay")
   public ResponseEntity<String> verifyPayment(@PathVariable String reference){
       orderService.finalizeTransaction(reference);
       return ResponseEntity.ok("verified successfully");
   }
   @PostMapping("/initiate/payment")
   public ResponseEntity<OrderResponse> initiatePayment()


}
