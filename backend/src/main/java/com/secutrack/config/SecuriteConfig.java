package com.secutrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration centrale de la sécurité.
 *
 * Spring Security fonctionne comme une chaîne de filtres : chaque requête HTTP traverse
 * ces filtres AVANT d'arriver au contrôleur. Ils répondent à deux questions :
 *   1. Authentification : qui es-tu ?       → vérification du JWT  (échec = 401 Unauthorized)
 *   2. Autorisation     : as-tu le droit ?  → vérification du rôle (échec = 403 Forbidden)
 */
@Configuration
// @EnableWebSecurity : active notre configuration de sécurité web personnalisée
@EnableWebSecurity
// @EnableMethodSecurity : active @PreAuthorize("hasRole('ADMIN')") sur les méthodes des contrôleurs
@EnableMethodSecurity
public class SecuriteConfig {

    /** URL accessibles sans être connecté. */
    private static final String[] ROUTES_PUBLIQUES = {
            "/api/auth/login", "/api/auth/register",                          // impossible d'exiger un jeton pour en obtenir un
            "/actuator/health/**",                                            // sonde de santé (Docker, Kubernetes)
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"           // documentation de l'API
    };

    /**
     * La chaîne de filtres de sécurité. Depuis Spring Security 6, on la configure
     * en déclarant un bean SecurityFilterChain (l'ancienne classe WebSecurityConfigurerAdapter n'existe plus).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF protège les applications qui utilisent des cookies de session.
            // Notre API est "stateless" (le jeton est envoyé dans un header) : on peut le désactiver.
            .csrf(csrf -> csrf.disable())
            // Autorise Angular (localhost:4200) à appeler l'API : voir corsConfigurationSource() plus bas
            .cors(Customizer.withDefaults())
            // STATELESS : Spring ne crée jamais de session HTTP ; chaque requête doit porter son JWT
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Règles d'autorisation par URL, lues dans l'ordre : la première qui correspond gagne
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ROUTES_PUBLIQUES).permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasRole("ADMIN")
                .requestMatchers("/api/utilisateurs/**").hasRole("ADMIN")
                .anyRequest().authenticated())                               // tout le reste : connecté
            // L'API joue le rôle de "resource server" : elle lit le header "Authorization: Bearer <jeton>",
            // vérifie le jeton avec le JwtDecoder (JwtConfig) puis applique le convertisseur de rôles
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            // Réponses en cas d'échec au niveau des filtres
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())   // pas de jeton valide → 401
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));           // mauvais rôle → 403
        return http.build();
    }

    /**
     * Notre JWT contient "roles": ["ADMIN"]. Spring, lui, raisonne en "autorités" préfixées : "ROLE_ADMIN".
     * Ce convertisseur fait le lien, ce qui permet d'écrire hasRole('ADMIN') dans les règles.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");     // nom de la claim à lire dans le jeton
        roles.setAuthorityPrefix("ROLE_");          // préfixe attendu par hasRole()
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    /**
     * Utilisé au login (AuthService) pour vérifier le couple courriel / mot de passe.
     * Spring le construit automatiquement à partir de notre UserDetailsService
     * (UtilisateurDetailsService) et du PasswordEncoder ci-dessous.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * BCrypt : algorithme de hachage conçu pour les mots de passe (lent volontairement, avec un "sel"
     * aléatoire). On ne stocke jamais un mot de passe en clair, seulement son hash.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS : par défaut, le navigateur bloque un appel de http://localhost:4200 (Angular)
     * vers http://localhost:8080 (API), car ce sont deux "origines" différentes.
     * On autorise explicitement les origines listées dans secutrack.cors-origines.
     * (Avec docker compose, nginx sert Angular et l'API sur la même origine : CORS n'intervient pas.)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecutrackProperties proprietes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(proprietes.corsOrigines());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
