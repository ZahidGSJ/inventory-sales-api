package com.zahid.inventoryapi.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class SaleRollbackIntegrationTest extends AbstractIntegrationTest {

        @MockitoBean
        private SaleRepository saleRepository;

        @Test
        void siFallaElGuardadoDeLaVenta_seRevierteElDescuentoDeStock() throws Exception {
                String admin = adminToken();
                long categoryId = createCategory(admin, "Electrónica");
                long productId = createProduct(admin, "LAP-001", "15000.00", categoryId);
                addStock(admin, productId, 10);

                when(saleRepository.save(any(Sale.class)))
                                .thenThrow(new IllegalStateException("Fallo simulado al guardar la venta"));

                assertThatThrownBy(() -> mockMvc.perform(post("/api/sales")
                                .header(AUTH_HEADER, bearer(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"items": [{"productId": %d, "quantity": 4}]}
                                                """.formatted(productId))))
                                .hasRootCauseMessage("Fallo simulado al guardar la venta");

                Integer stock = jdbcTemplate.queryForObject("SELECT stock FROM products WHERE id = ?", Integer.class,
                                productId);
                Long salidas = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM inventory_movements WHERE type = 'SALIDA'", Long.class);

                assertThat(stock).isEqualTo(10);
                assertThat(salidas).isZero();
        }
}