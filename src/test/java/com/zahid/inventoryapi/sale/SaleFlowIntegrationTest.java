package com.zahid.inventoryapi.sale;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SaleFlowIntegrationTest extends AbstractIntegrationTest {

    private long productWithStock(String admin, int stock) throws Exception {
        long categoryId = createCategory(admin, "Electrónica");
        long productId = createProduct(admin, "LAP-001", "15000.00", categoryId);
        addStock(admin, productId, stock);
        return productId;
    }

    @Test
    void ventaDeUnVendedor_descuentaElStockYDejaElHistorialCoherente() throws Exception {
        String admin = adminToken();
        String vendedor = createVendedorAndLogin(admin, "ana@test.local", "ventas1234");
        long productId = productWithStock(admin, 10);

        mockMvc.perform(post("/api/sales")
                .header(AUTH_HEADER, bearer(vendedor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"items": [{"productId": %d, "quantity": 2}]}
                        """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(30000.0))
                .andExpect(jsonPath("$.items.length()").value(1));

        mockMvc.perform(get("/api/products/" + productId).header(AUTH_HEADER, bearer(admin)))
                .andExpect(jsonPath("$.stock").value(8));

        List<String> movements = jdbcTemplate.queryForList(
                "SELECT type || ':' || quantity FROM inventory_movements WHERE product_id = ? ORDER BY id",
                String.class, productId);
        assertThat(movements).containsExactly("ENTRADA:10", "SALIDA:2");
    }

    @Test
    void mismoProductoEnDosLineas_guardaDosItemsYUnaSolaSalida() throws Exception {
        String admin = adminToken();
        long productId = productWithStock(admin, 10);

        mockMvc.perform(post("/api/sales")
                .header(AUTH_HEADER, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"items": [{"productId": %d, "quantity": 3}, {"productId": %d, "quantity": 3}]}
                        """.formatted(productId, productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(2));

        List<Integer> salidas = jdbcTemplate.queryForList(
                "SELECT quantity FROM inventory_movements WHERE type = 'SALIDA'", Integer.class);
        assertThat(salidas).containsExactly(6);
    }

    @Test
    void ventaConStockInsuficiente_devuelve409YNoDejaRastro() throws Exception {
        String admin = adminToken();
        long productId = productWithStock(admin, 5);

        mockMvc.perform(post("/api/sales")
                .header(AUTH_HEADER, bearer(admin))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"items": [{"productId": %d, "quantity": 8}]}
                        """.formatted(productId)))
                .andExpect(status().isConflict());

        Long sales = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sales", Long.class);
        Integer stock = jdbcTemplate.queryForObject("SELECT stock FROM products WHERE id = ?", Integer.class,
                productId);
        assertThat(sales).isZero();
        assertThat(stock).isEqualTo(5);
    }
}