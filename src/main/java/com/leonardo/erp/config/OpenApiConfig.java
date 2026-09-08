package com.leonardo.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI erpBackendOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ERP Backend API")
                        .description("Order management module: catalog items, orders and order items")
                        .version("v1"));
    }
}
