package com.zahid.inventoryapi.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank String name,

        String description) {
}