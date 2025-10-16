package com.tripgether.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tripgetherOpenAPI() {
        SecurityScheme bearerAuth =
                new SecurityScheme().type(
                        SecurityScheme.Type
                                .HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(
                                SecurityScheme.In
                                        .HEADER)
                        .name("Authorization");

        return new OpenAPI().info(
                new Info().title("Tripgether API")
                        .description("Tripgether의 REST API 문서")
                        .version("v1.0.0")
                        .contact(new Contact().name("Tripgether Team")))
                .components(new Components().addSecuritySchemes("Bearer Token", bearerAuth))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"));
    }
}
