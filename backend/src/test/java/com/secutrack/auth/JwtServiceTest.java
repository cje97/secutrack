package com.secutrack.auth;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.secutrack.config.JwtConfig;
import com.secutrack.utilisateur.Role;
import com.secutrack.utilisateur.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Test unitaire pur : vrais encodeur et décodeur Nimbus, aucun contexte Spring. */
class JwtServiceTest {

    private final SecretKey cle = JwtConfig.creerCle("une-cle-de-test-suffisamment-longue-pour-hs256");
    private final JwtService service = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(cle)), 60);
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(cle).macAlgorithm(MacAlgorithm.HS256).build();

    // Un JWT généré puis décodé doit contenir exactement les informations de l'utilisateur
    @Test
    void leJetonContientLeCourrielEtLeRole() {
        Utilisateur admin = new Utilisateur("Alice Admin", "admin@secutrack.local", "hash", Role.ADMIN);

        JetonGenere jeton = service.generer(admin);
        Jwt decode = decoder.decode(jeton.valeur());

        assertThat(decode.getSubject()).isEqualTo("admin@secutrack.local");
        assertThat(decode.getClaimAsStringList("roles")).isEqualTo(List.of("ADMIN"));
        assertThat(decode.getClaimAsString("nom")).isEqualTo("Alice Admin");
        assertThat(decode.getExpiresAt()).isEqualTo(jeton.expiration());
    }

    // Test de sécurité clé : un attaquant qui fabrique un jeton ADMIN avec sa propre clé est refusé
    @Test
    void unJetonSigneAvecUneAutreCleEstRefuse() {
        SecretKey autreCle = JwtConfig.creerCle("une-autre-cle-de-test-tout-aussi-longue-hs256");
        JwtService autreService = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(autreCle)), 60);
        Utilisateur pirate = new Utilisateur("Pirate", "pirate@exemple.com", "hash", Role.ADMIN);

        String jetonFalsifie = autreService.generer(pirate).valeur();

        assertThatThrownBy(() -> decoder.decode(jetonFalsifie)).isInstanceOf(JwtException.class);
    }

    @Test
    void uneCleTropCourteEstRefuseeAuDemarrage() {
        assertThatThrownBy(() -> JwtConfig.creerCle("trop-courte"))
                .isInstanceOf(IllegalStateException.class);
    }
}
