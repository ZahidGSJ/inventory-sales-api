package com.zahid.inventoryapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.zahid.inventoryapi.support.AbstractIntegrationTest;

class DatabaseSchemaIntegrationTest extends AbstractIntegrationTest {

        @Test
        void lasMigracionesSeAplicanSinErrores() {
                List<String> versions = jdbcTemplate.queryForList(
                                "SELECT version FROM flyway_schema_history WHERE success = true ORDER BY installed_rank",
                                String.class);
                Integer failed = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = false", Integer.class);

                assertThat(versions).contains("1", "2");
                assertThat(failed).isZero();
        }

        @Test
        void losRolesInicialesLosInsertaLaMigracion() {
                List<String> roles = jdbcTemplate.queryForList("SELECT name FROM roles ORDER BY name", String.class);

                assertThat(roles).containsExactly("ADMIN", "VENDEDOR");
        }

        @Test
        void elAdministradorInicialSeCreaConLaContrasenaCifrada() {
                String hash = jdbcTemplate.queryForObject(
                                "SELECT password_hash FROM users WHERE email = ?", String.class, ADMIN_EMAIL);

                assertThat(hash).startsWith("$2").isNotEqualTo(ADMIN_PASSWORD);
        }

        @Test
        void laBaseDeDatosRechazaEmailsEnMayusculas() {
                Long roleId = jdbcTemplate.queryForObject(
                                "SELECT id FROM roles WHERE name = ?",
                                Long.class,
                                "VENDEDOR");

                assertThat(roleId).isNotNull();

                assertThatThrownBy(() -> jdbcTemplate.update(
                                """
                                                INSERT INTO users (name, email, password_hash, role_id, active)
                                                VALUES (?, ?, ?, ?, ?)
                                                """,
                                "Usuario de prueba",
                                "USUARIO@INVENTARIO.LOCAL",
                                "$2b$12$hashDePrueba",
                                roleId,
                                true)).isInstanceOf(DataIntegrityViolationException.class)
                                .hasMessageContaining("chk_users_email_lowercase");
        }

        @Test
        void laBaseDeDatosAceptaEmailsEnMinusculas() {
                Long roleId = jdbcTemplate.queryForObject("SELECT id FROM roles WHERE name = ?", Long.class,
                                "VENDEDOR");

                int inserted = jdbcTemplate.update(
                                "INSERT INTO users (name, email, password_hash, role_id, active) VALUES (?, ?, ?, ?, ?)",
                                "Usuario de prueba", "usuario@inventario.local", "$2b$12$hashDePrueba", roleId, true);

                assertThat(inserted).isEqualTo(1);
        }
}