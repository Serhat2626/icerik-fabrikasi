package com.icerikfabrikasi.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger konfigürasyonu.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI icerikFabrikasiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("İçerik Fabrikası API")
                        .version("0.1.0")
                        .description("İçerik Fabrikası - AI destekli çok kanallı içerik üretim platformu REST API")
                        .license(new License()
                                .name("Tescilli")
                                .url("https://icerikfabrikasi.com")));
    }
}
