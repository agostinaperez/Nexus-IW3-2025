package edu.iua.nexus.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Nexus API - Ingeniería Web 3",
        version = "v1.0",
        description = "Documentación de la API para el proyecto integrador Nexus."
    ),
    // Esto añade el candado "Authorize" a todos los endpoints
    security = @SecurityRequirement(name = "Bearer Authentication") 
)
@SecurityScheme(
    // Esto define CÓMO es la seguridad
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class OpenApiConfig {

}
