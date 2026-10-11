package com.zahid.inventoryapi.sale;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal) {

    public static SaleItemResponse from(SaleItem item) {
        return new SaleItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.calculateSubTotal());
    }
}
