package com.sprint.findex_team6.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Findex API 문서")
            .description("Findex-Team6 Swagger API 문서")
        )
        .servers(List.of(
            new Server().url("http://localhost:8080").description("로컬서버")
        ));
  }

}
