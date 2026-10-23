package com.zahid.inventoryapi.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRequest(
        @NotBlank String name,
        @NotBlank String role,
        @NotNull Boolean active) {
}