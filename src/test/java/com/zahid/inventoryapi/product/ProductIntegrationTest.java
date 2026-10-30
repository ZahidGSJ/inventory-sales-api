package com.zahid.inventoryapi.product;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class ProductIntegrationTest extends AbstractIntegrationTest {

    @Test
    void laBaseDeDatosRechazaSkuQueSoloDifierenEnMayusculas() {
        Long categoryId = jdbcTemplate.queryForObject(
                "INSERT INTO categories (name) VALUES (?) RETURNING id",
                Long.class,
                "Accesorios");

        jdbcTemplate.update("""
                INSERT INTO products
                    (sku, name, price, stock, minimum_stock, active, category_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                "LAP-001",
                "Laptop Lenovo",
                15000.00,
                0,
                5,
                true,
                categoryId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO products
                    (sku, name, price, stock, minimum_stock, active, category_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                "lap-001",
                "Otra laptop",
                12000.00,
                0,
                5,
                true,
                categoryId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uk_products_sku_lower");
    }

    @Test
    void unSkuQueSoloDifiereEnMayusculas_devuelve409() throws Exception {
        String admin = adminToken();
        long categoryId = createCategory(admin, "Accesorios");
        createProduct(admin, "LAP-001", "15000.00", categoryId);

        mockMvc.perform(post("/api/products")
                .header(AUTH_HEADER, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku": " lap-001 ", "name": "Otra laptop", "price": 12000.00,
                         "minimumStock": 5, "categoryId": %d}
                        """.formatted(categoryId)))
                .andExpect(status().isConflict());
    }
}