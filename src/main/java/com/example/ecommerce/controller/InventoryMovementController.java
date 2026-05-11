package com.example.ecommerce.controller;

import com.example.ecommerce.model.InventoryMovement;
import com.example.ecommerce.service.InventoryMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/inventoryMovements")
public class InventoryMovementController {
    @Autowired
    private InventoryMovementService inventoryMovementService;

    @GetMapping
    public List<InventoryMovement> getInventory(){
        return inventoryMovementService.getAllMovements();
    }

}
