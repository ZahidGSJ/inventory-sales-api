package com.zahid.inventoryapi.category;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class CategoryIntegrationTest extends AbstractIntegrationTest {

        @Test
        void actualizarConElNombreDeOtraCategoria_devuelve409YNoUn500() throws Exception {
                String admin = adminToken();
                createCategory(admin, "Electrónica");
                long accesorios = createCategory(admin, "Accesorios");

                mockMvc.perform(put("/api/categories/" + accesorios)
                                .header(AUTH_HEADER, bearer(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name": "Electrónica", "description": "x"}
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.message").value(containsString("Electrónica")));
        }

        @Test
        void actualizarConsuPropioNombre_cambiandoSoloLaDescripcion_esValido() throws Exception {
                String admin = adminToken();
                long accesorios = createCategory(admin, "Accesorios");

                mockMvc.perform(put("/api/categories/" + accesorios)
                                .header(AUTH_HEADER, bearer(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name": "Accesorios", "description": "Descripción nueva"}
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.description").value("Descripción nueva"));
        }

        @Test
        void unNombreQueSoloDifiereEnMayusculas_devuelve409() throws Exception {
                String admin = adminToken();
                createCategory(admin, "Accesorios");

                mockMvc.perform(post("/api/categories")
                                .header(AUTH_HEADER, bearer(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name": " ACCESORIOS ", "description": "x"}
                                                """))
                                .andExpect(status().isConflict());
        }

        @Test
        void laBaseDeDatosRechazaNombresQueSoloDifierenEnMayusculas() {
                jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", "Accesorios");

                assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", "ACCESORIOS"))
                                .isInstanceOf(DataIntegrityViolationException.class)
                                .hasMessageContaining("uk_categories_name_lower");
        }
}