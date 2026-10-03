package com.zahid.inventoryapi.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryMovementRequest(
        @NotNull Long productId,

        @NotNull MovementType type,

        @NotNull Integer quantity,

        @NotBlank String reason) {
}