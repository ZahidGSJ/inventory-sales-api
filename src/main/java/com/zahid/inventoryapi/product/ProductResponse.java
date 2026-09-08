package com.zahid.inventoryapi.product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        Integer minimumStock,
        Boolean active,
        Long categoryId,
        String categoryName) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getMinimumStock(),
                product.getActive(),
                product.getCategory().getId(),
                product.getCategory().getName());
    }
}