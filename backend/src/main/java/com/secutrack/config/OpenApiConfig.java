package com.secutrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentation de l'API (OpenAPI 3), générée automatiquement par springdoc
 * à partir des contrôleurs. Consultable sur /swagger-ui.html.
 *
 * Ce bean ajoute le titre de l'API et le bouton "Authorize" dans Swagger UI :
 * on y colle le jeton reçu au login pour tester les endpoints protégés.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secutrackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SecuTrack API")
                        .description("Gestion des tickets de sécurité : incidents, vulnérabilités, demandes.")
                        .version("1.0.0"))
                // Déclare le mode d'authentification "Bearer JWT"...
                .components(new Components().addSecuritySchemes("jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                // ...et indique qu'il s'applique à tous les endpoints
                .addSecurityItem(new SecurityRequirement().addList("jwt"));
    }
}
