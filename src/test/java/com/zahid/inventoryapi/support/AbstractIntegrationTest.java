package com.zahid.inventoryapi.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.jwt.secret=Y2xhdmUtZGUtcHJ1ZWJhLXNvbG8tcGFyYS10ZXN0cyE=",
        "app.jwt.expiration-minutes=15",
        "app.admin.name=Admin Test",
        "app.admin.email=admin@test.local",
        "app.admin.password=Admin12345"
})
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    protected static final String ADMIN_EMAIL = "admin@test.local";
    protected static final String ADMIN_PASSWORD = "Admin12345";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    protected void cleanDatabase() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE sale_items, sales, inventory_movements, products, categories RESTART IDENTITY CASCADE");
        jdbcTemplate.update("DELETE FROM users WHERE email <> ?", ADMIN_EMAIL);
    }
}