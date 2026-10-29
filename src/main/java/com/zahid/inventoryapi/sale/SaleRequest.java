package com.zahid.inventoryapi.sale;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record SaleRequest(
        @NotEmpty @Valid List<SaleItemRequest> items) {
}
