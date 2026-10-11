package com.zahid.inventoryapi.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        LocalDateTime createdAt,
        BigDecimal total,
        List<SaleItemResponse> items) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getCreatedAt(),
                sale.getTotal(),
                sale.getItems().stream().map(SaleItemResponse::from).toList());
    }
}
