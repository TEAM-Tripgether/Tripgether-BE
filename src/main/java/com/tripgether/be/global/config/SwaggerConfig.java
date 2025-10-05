package com.tripgether.be.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tripgetherOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tripgether API")
                        .description("Tripgether의 REST API 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Tripgether Team")));
    }
}
