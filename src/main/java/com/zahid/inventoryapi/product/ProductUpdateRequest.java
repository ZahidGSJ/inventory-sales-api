package com.zahid.inventoryapi.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductUpdateRequest(
                @NotBlank String name,

                String description,

                @NotNull @Positive BigDecimal price,

                @NotNull @PositiveOrZero Integer minimumStock,

                @NotNull Boolean active,

                @NotNull Long categoryId) {
}