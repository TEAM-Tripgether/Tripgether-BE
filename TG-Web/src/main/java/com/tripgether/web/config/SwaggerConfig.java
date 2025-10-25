package com.tripgether.web.config;

import com.tripgether.common.properties.SpringDocProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @io.swagger.v3.oas.annotations.info.Info(
        title = "📚 Tripgether : 여행의 동반자 📚",
        description = """
            ### http://suh-project.synology.me:8093
            """
    )
)
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(SpringDocProperties.class)
public class SwaggerConfig {

  private final SpringDocProperties springDocProperties;

  @Bean
  public OpenAPI tripgetherOpenAPI() {
    SecurityScheme bearerAuth = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
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
                .url("https://api.test.tripgether.suhsaechan.kr")
                .description("테스트 서버"),
            new io.swagger.v3.oas.models.servers.Server()
                .url("http://localhost:8080")
                .description("로컬 서버")
        ));
  }

//  @Bean
//  public OpenApiCustomizer serverCustomizer() {
//    return openApi -> {
//      springDocProperties.getServers().forEach(server ->
//          openApi.addServersItem(new io.swagger.v3.oas.models.servers.Server()
//              .url(server.getUrl())
//              .description(server.getDescription()))
//      );
//    };
//  }
}
