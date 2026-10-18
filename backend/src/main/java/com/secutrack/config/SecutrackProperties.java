package com.secutrack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propriétés propres à SecuTrack, lues dans application.yml :
 *
 * <pre>
 * secutrack:
 *   jwt:
 *     secret: ...            → jwt().secret()
 *     duree-minutes: 120     → jwt().dureeMinutes()   (Spring convertit kebab-case → camelCase)
 *   demo:
 *     actif: true            → demo().actif()
 *   cors-origines: [...]     → corsOrigines()
 * </pre>
 *
 * Avantage par rapport à plusieurs @Value("${...}") : tout est regroupé et typé dans un seul objet.
 */
// @ConfigurationProperties(prefix = "secutrack") : Spring remplit ce record avec toutes les
// propriétés qui commencent par "secutrack." (fichier YAML, variables d'environnement...)
@ConfigurationProperties(prefix = "secutrack")
public record SecutrackProperties(Jwt jwt, Demo demo, List<String> corsOrigines) {

    /** Bloc secutrack.jwt : clé de signature et durée de validité des jetons. */
    public record Jwt(String secret, long dureeMinutes) {
    }

    /** Bloc secutrack.demo : création des comptes et tickets de démonstration au démarrage. */
    public record Demo(boolean actif, String motDePasse) {
    }
}
