package com.zahid.inventoryapi.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class AuthorizationIntegrationTest extends AbstractIntegrationTest {

        @Test
        void sinToken_laRutaProtegidaDevuelve401() throws Exception {
                mockMvc.perform(get("/api/products"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message")
                                                .value("Se requiere un token válido en la cabecera Authorization"));
        }

        @Test
        void tokenAlterado_devuelve401() throws Exception {
                String token = adminToken();

                mockMvc.perform(get("/api/products").header(AUTH_HEADER, bearer(token + "x")))
                                .andExpect(status().isUnauthorized());
        }

        @ParameterizedTest(name = "login con {0} y esa contraseña falla con el mismo mensaje")
        @CsvSource({
                        "admin@test.local, contrasena-equivocada",
                        "nadie@test.local, loquesea123"
        })
        void loginFallido_devuelveSiempreElMismoMensaje(String email, String password) throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "%s", "password": "%s"}
                                                """.formatted(email, password)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
        }

        @Test
        void admin_puedeListarUsuarios() throws Exception {
                mockMvc.perform(get("/api/users").header(AUTH_HEADER, bearer(adminToken())))
                                .andExpect(status().isOk());
        }

        @Test
        void vendedor_puedeConsultarProductos() throws Exception {
                String vendedor = createVendedorAndLogin(adminToken(), "ana@test.local", "ventas1234");

                mockMvc.perform(get("/api/products").header(AUTH_HEADER, bearer(vendedor)))
                                .andExpect(status().isOk());
        }

        @ParameterizedTest(name = "el VENDEDOR no puede {0} {1}")
        @CsvSource({
                        "POST, /api/products",
                        "PUT, /api/products/1",
                        "POST, /api/categories",
                        "PUT, /api/categories/1",
                        "POST, /api/inventory-movements",
                        "GET, /api/inventory-movements/product/1",
                        "GET, /api/users",
                        "POST, /api/users"
        })
        void vendedor_noPuedeUsarOperacionesAdministrativas(String method, String url) throws Exception {
                String vendedor = createVendedorAndLogin(adminToken(), "ana@test.local", "ventas1234");

                mockMvc.perform(request(HttpMethod.valueOf(method), url)
                                .header(AUTH_HEADER, bearer(vendedor))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message")
                                                .value("No tienes permisos para realizar esta operación"));
        }

        @Test
        void usuarioDesactivado_suTokenDejaDeFuncionarDeInmediato() throws Exception {
                String admin = adminToken();
                String vendedor = createVendedorAndLogin(admin, "ana@test.local", "ventas1234");
                Long anaId = jdbcTemplate.queryForObject(
                                "SELECT id FROM users WHERE email = ?", Long.class, "ana@test.local");

                mockMvc.perform(put("/api/users/" + anaId)
                                .header(AUTH_HEADER, bearer(admin))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name": "Ana", "role": "VENDEDOR", "active": false}
                                                """))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/api/products").header(AUTH_HEADER, bearer(vendedor)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void rutaInexistente_conTokenDeAdmin_devuelve403() throws Exception {
                mockMvc.perform(get("/api/ruta-que-no-existe").header(AUTH_HEADER, bearer(adminToken())))
                                .andExpect(status().isForbidden());
        }
}