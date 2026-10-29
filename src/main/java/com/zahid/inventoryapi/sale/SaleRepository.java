package com.zahid.inventoryapi.sale;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    @EntityGraph(attributePaths = { "items", "items.product", "items.product.category" })
    @Query("select s from Sale s order by s.createdAt desc")
    List<Sale> findAllWithItems();
}
