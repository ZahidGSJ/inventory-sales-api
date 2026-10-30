package com.zahid.inventoryapi.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select count(p) > 0 from Product p where lower(p.sku) = lower(:sku)")
    boolean existsBySkuIgnoreCase(@Param("sku") String sku);
}