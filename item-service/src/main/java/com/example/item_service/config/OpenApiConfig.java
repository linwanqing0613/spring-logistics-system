package com.example.item_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Item Service API",
                version = "v1",
                description = "API Documentation for Item Service"
        )
)
@ComponentScan(basePackages = "com.example")
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("item-service")
                .packagesToScan("com.example.item_service")
                .build();
    }
}