package com.secutrack.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuration des JWT (JSON Web Token).
 *
 * Un JWT a trois parties séparées par des points : en-tête.contenu.signature
 *   - en-tête   : l'algorithme de signature (ici HS256)
 *   - contenu   : les "claims" (courriel, rôles, date d'expiration...)
 *   - signature : calculée avec la clé secrète. Si quelqu'un modifie le contenu
 *                 (par exemple pour se donner le rôle ADMIN), la signature ne correspond plus
 *                 et le jeton est refusé.
 *
 * HS256 est un algorithme symétrique : la même clé secrète sert à signer ET à vérifier.
 * La bibliothèque Nimbus est fournie par Spring Security (aucune dépendance en plus).
 */
// @Configuration : classe qui déclare des beans avec des méthodes @Bean
@Configuration
public class JwtConfig {

    // @Bean : l'objet retourné devient un bean géré par Spring, injectable partout.
    // On utilise @Bean (et pas @Component) car SecretKey, JwtEncoder et JwtDecoder
    // sont des classes de bibliothèques qu'on ne peut pas annoter nous-mêmes.
    @Bean
    public SecretKey cleJwt(SecutrackProperties proprietes) {
        return creerCle(proprietes.jwt().secret());
    }

    /** Signe les jetons au login (utilisé par JwtService). */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey cleJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(cleJwt));
    }

    /**
     * Vérifie les jetons à chaque requête : signature valide + date d'expiration non dépassée.
     * Spring Security l'utilise automatiquement grâce à .oauth2ResourceServer() dans SecuriteConfig.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey cleJwt) {
        return NimbusJwtDecoder.withSecretKey(cleJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Construit la clé à partir du secret de la configuration.
     * Méthode statique pour pouvoir l'utiliser dans un test unitaire, sans démarrer Spring.
     */
    public static SecretKey creerCle(String secret) {
        byte[] octets = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 exige une clé d'au moins 256 bits = 32 octets. On bloque le démarrage sinon.
        if (octets.length < 32) {
            throw new IllegalStateException("secutrack.jwt.secret doit contenir au moins 32 caractères");
        }
        return new SecretKeySpec(octets, "HmacSHA256");
    }
}
