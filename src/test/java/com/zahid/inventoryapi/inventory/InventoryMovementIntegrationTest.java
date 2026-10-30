package com.zahid.inventoryapi.inventory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class InventoryMovementIntegrationTest extends AbstractIntegrationTest {

    @Test
    void elHistorialSeDevuelveDelMasRecienteAlMasAntiguo() throws Exception {
        String admin = adminToken();
        long categoryId = createCategory(admin, "Electrónica");
        long productId = createProduct(admin, "LAP-001", "15000.00", categoryId);
        addStock(admin, productId, 10);
        addStock(admin, productId, 20);
        addStock(admin, productId, 30);

        mockMvc.perform(get("/api/inventory-movements/product/" + productId)
                .header(AUTH_HEADER, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].quantity").value(30))
                .andExpect(jsonPath("$[1].quantity").value(20))
                .andExpect(jsonPath("$[2].quantity").value(10));
    }
}