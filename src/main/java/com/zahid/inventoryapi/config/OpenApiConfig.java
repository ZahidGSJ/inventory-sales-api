package com.zahid.inventoryapi.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME = "bearerAuth";

        private static final List<String> TAG_ORDER = List.of(
                        "Autenticación", "Categorías", "Productos", "Inventario", "Ventas", "Usuarios");

        @Bean
        public OpenAPI inventoryOpenApi() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Inventory & Sales Management API")
                                                .version("1.0.0")
                                                .description("API REST para gestionar productos, inventario, ventas y usuarios, con autenticación JWT y autorización por roles"))
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME, new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")));
        }

        @Bean
        public OpenApiCustomizer tagOrderCustomizer() {
                return openApi -> {
                        if (openApi.getTags() == null) {
                                return;
                        }
                        List<Tag> sorted = new ArrayList<>(openApi.getTags());
                        sorted.sort(Comparator.comparingInt((Tag tag) -> {
                                int index = TAG_ORDER.indexOf(tag.getName());
                                return index < 0 ? Integer.MAX_VALUE : index;
                        }));
                        openApi.setTags(sorted);
                };
        }
}