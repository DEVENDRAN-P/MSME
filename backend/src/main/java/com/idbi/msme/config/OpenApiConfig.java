package com.idbi.msme.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MSME Financial Intelligence Platform API")
                        .description("Enterprise AI platform for MSME credit assessment using alternate data streams")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IDBI Bank Hackathon Team")
                                .email("hackathon@idbi.co.in")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .schemaRequirement("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Firebase ID Token")
                                .description("Firebase ID Token obtained from Firebase Authentication"));
    }
}
