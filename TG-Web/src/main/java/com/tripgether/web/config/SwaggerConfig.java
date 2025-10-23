package com.tripgether.web.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @io.swagger.v3.oas.annotations.info.Info(
        title = "📚 Tripgether : 여행의 동반자 📚",
        description = """
            ### http://suh-project.synology.me:8093
            """
    ),
    servers = {
        @Server(url = "https://api.tripgether.suhsaechan.kr", description = "메인 서버"),
        @Server(url = "http://localhost:8080", description = "로컬 서버")
    }
)
public class SwaggerConfig {

  @Bean
  public OpenAPI tripgetherOpenAPI() {
    SecurityScheme bearerAuth =
        new SecurityScheme().type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", bearerAuth))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
        .servers(List.of(
            new io.swagger.v3.oas.models.servers.Server()
                .url("https://api.tripgether.suhsaechan.kr")
                .description("메인 서버"),
            new io.swagger.v3.oas.models.servers.Server()
                .url("http://localhost:8080")
                .description("로컬 서버")
        ));
  }
}
