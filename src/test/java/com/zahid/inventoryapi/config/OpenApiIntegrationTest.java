package com.zahid.inventoryapi.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class OpenApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void laDocumentacionEsPublicaYDescribeLosEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Inventory & Sales Management API"))
                .andExpect(jsonPath("$.paths['/api/products']").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login']").exists());
    }
}