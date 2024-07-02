package com.fp.ProductService.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerOpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0")
                        .description("API documentation for Order Service")
                        .termsOfService("http://swagger.io/terms/")
                        .contact(new Contact().name("Your Name").email("your.email@example.com").url("http://yourwebsite.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}