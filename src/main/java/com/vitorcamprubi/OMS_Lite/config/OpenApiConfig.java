package com.vitorcamprubi.OMS_Lite.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI omsLiteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OMS-Lite API")
                        .description("Mini Order Management System — cadastro de clientes/produtos e criação de pedidos com baixa de estoque.")
                        .version("v0.1")
                        .contact(new Contact()
                                .name("Vitor Camprubi")
                                .url("https://github.com/VitorCamprubi/OMS-Lite"))
                        .license(new License().name("MIT")));
    }
}
