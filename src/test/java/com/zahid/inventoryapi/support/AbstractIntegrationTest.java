package com.zahid.inventoryapi.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

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

    protected static final String AUTH_HEADER = "Authorization";

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    protected String adminToken() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected long postAndGetId(String url, String token, String json) throws Exception {
        MvcResult result = mockMvc.perform(post(url)
                .header(AUTH_HEADER, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    protected String createVendedorAndLogin(String adminToken, String email, String password) throws Exception {
        postAndGetId("/api/users", adminToken, """
                {"name": "Vendedor Test", "email": "%s", "password": "%s", "role": "VENDEDOR"}
                """.formatted(email, password));
        return login(email, password);
    }

    protected long createCategory(String token, String name) throws Exception {
        return postAndGetId("/api/categories", token, """
                {"name": "%s", "description": "Categoría de prueba"}
                """.formatted(name));
    }

    protected long createProduct(String token, String sku, String price, long categoryId) throws Exception {
        return postAndGetId("/api/products", token,
                """
                        {"sku": "%s", "name": "Producto %s", "description": "De prueba", "price": %s, "minimumStock": 2, "categoryId": %d}
                        """
                        .formatted(sku, sku, price, categoryId));
    }

    protected void addStock(String token, long productId, int quantity) throws Exception {
        postAndGetId("/api/inventory-movements", token, """
                {"productId": %d, "type": "ENTRADA", "quantity": %d, "reason": "Carga de prueba"}
                """.formatted(productId, quantity));
    }
}