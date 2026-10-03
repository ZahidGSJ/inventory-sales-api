package com.zahid.inventoryapi.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory-movements")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    public InventoryMovementController(InventoryMovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    public ResponseEntity<InventoryMovementResponse> registerMovement(
            @Valid @RequestBody InventoryMovementRequest request) {

        InventoryMovementResponse response = movementService.registerMovement(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryMovementResponse>> getMovementsByProduct(
            @PathVariable Long productId) {

        List<InventoryMovementResponse> movements =
                movementService.getMovementsByProduct(productId);

        return ResponseEntity.ok(movements);
    }
}