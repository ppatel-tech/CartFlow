package com.cartflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CartFlow API")
                        .description("Enterprise E-Commerce Backend REST API Documentation")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("CartFlow Backend Team")
                                .email("support@cartflow.com")));
    }
}