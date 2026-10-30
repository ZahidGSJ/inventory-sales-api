package com.zahid.inventoryapi.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class ErrorHandlingIntegrationTest extends AbstractIntegrationTest {

    private static final String UNREADABLE_BODY_MESSAGE = "El cuerpo de la petición falta, no es un JSON válido o contiene valores no permitidos";

    @ParameterizedTest(name = "cuerpo no legible: {0}")
    @ValueSource(strings = {
            "{ esto no es json",
            "{\"productId\": 1, \"type\": \"OTRO\", \"quantity\": 1, \"reason\": \"x\"}",
            "{\"productId\": \"abc\", \"type\": \"ENTRADA\", \"quantity\": 1, \"reason\": \"x\"}"
    })
    void cuerpoNoLegible_devuelveElErrorResponseDelProyecto(String body) throws Exception {
        mockMvc.perform(post("/api/inventory-movements")
                .header(AUTH_HEADER, bearer(adminToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(UNREADABLE_BODY_MESSAGE));
    }

    @Test
    void peticionSinCuerpo_devuelveElErrorResponseDelProyecto() throws Exception {
        mockMvc.perform(post("/api/inventory-movements")
                .header(AUTH_HEADER, bearer(adminToken()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(UNREADABLE_BODY_MESSAGE));
    }
}