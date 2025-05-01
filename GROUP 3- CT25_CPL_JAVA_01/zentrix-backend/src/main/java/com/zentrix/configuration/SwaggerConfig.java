package com.zentrix.configuration;

import java.util.List;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/*
 * @author Le Nhut Anh - CE181767 - CT25_CPL_JAVA_01
 * @date  February 13, 2025
 */

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI openAPI(@Value("${open.api.title}") String title, @Value("${open.api.version}") String version,
                        @Value("${open.api.description}") String description,
                        @Value("${open.api.serverUrl}") String serverUrl,
                        @Value("${open.api.serverName}") String serverName) {
                return new OpenAPI().info(new Info().title(title).version(version).description(description)
                // .license(new License().name("Zentrix API
                // License").url("http://localhost:8080/license"))
                ).servers(List.of(new Server().url(serverUrl).description("Zentrix server")))
                                .components(new Components().addSecuritySchemes("Authorization",
                                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                                                                .bearerFormat("JWT")))
                                .security(List.of(new SecurityRequirement().addList("Authorization")));
        }

        @Bean
        public GroupedOpenApi groupedOpenApi() {
                return GroupedOpenApi.builder().group("api-service").packagesToScan("com.zentrix.controller").build();
        }

}
