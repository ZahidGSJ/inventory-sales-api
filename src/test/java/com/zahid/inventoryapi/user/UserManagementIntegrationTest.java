package com.zahid.inventoryapi.user;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserManagementIntegrationTest extends AbstractIntegrationTest {

    private Long adminId() {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, ADMIN_EMAIL);
    }

    private ResultActions updateUser(String token, Long id, String name, String role, boolean active)
            throws Exception {
        return mockMvc.perform(put("/api/users/" + id)
                .header(AUTH_HEADER, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s", "role": "%s", "active": %b}
                        """.formatted(name, role, active)));
    }

    @Test
    void admin_noPuedeDesactivarseASiMismo_yPermaneceActivo() throws Exception {
        Long adminId = adminId();

        updateUser(adminToken(), adminId, "Administrador", "ADMIN", false)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No puedes desactivar tu propia cuenta"));

        Boolean active = jdbcTemplate.queryForObject(
                "SELECT active FROM users WHERE id = ?", Boolean.class, adminId);
        assertThat(active).isTrue();
    }

    @Test
    void admin_noPuedeCambiarSuPropioRol_yConservaElDeAdministrador() throws Exception {
        Long adminId = adminId();

        updateUser(adminToken(), adminId, "Administrador", "VENDEDOR", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No puedes cambiar tu propio rol"));

        String role = jdbcTemplate.queryForObject(
                "SELECT r.name FROM users u JOIN roles r ON r.id = u.role_id WHERE u.id = ?",
                String.class, adminId);
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    void admin_siPuedeCambiarElRolDeOtroUsuario() throws Exception {
        String admin = adminToken();
        createVendedorAndLogin(admin, "ana@test.local", "ventas1234");
        Long anaId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, "ana@test.local");

        updateUser(admin, anaId, "Ana", "ADMIN", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}