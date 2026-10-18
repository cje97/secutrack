package com.secutrack.auth;

import com.secutrack.config.SecutrackProperties;
import com.secutrack.utilisateur.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Génère le JWT renvoyé au login. Contenu (claims) du jeton :
 * <pre>
 * {
 *   "iss": "secutrack",                 émetteur
 *   "sub": "admin@secutrack.local",     sujet = identifiant de l'utilisateur
 *   "roles": ["ADMIN"],                 lu par le convertisseur de SecuriteConfig
 *   "nom": "Alice Admin",
 *   "iat": 1760000000,                  émis le (secondes depuis 1970)
 *   "exp": 1760007200                   expire le
 * }
 * </pre>
 * Attention : le contenu d'un JWT est encodé (Base64), PAS chiffré. N'importe qui peut le lire
 * sur jwt.io. Il ne faut donc jamais y mettre d'information secrète. La signature garantit
 * seulement qu'il n'a pas été modifié.
 */
// @Service : bean Spring de la couche métier
@Service
public class JwtService {

    public static final String EMETTEUR = "secutrack";

    private final JwtEncoder encoder;
    private final long dureeMinutes;

    /**
     * Constructeur utilisé par Spring : il fournit les deux beans automatiquement.
     *
     * @Autowired est OBLIGATOIRE ici : la classe a deux constructeurs, Spring doit savoir lequel utiliser.
     * Sans lui, Spring cherche un constructeur vide, n'en trouve pas, et l'application ne démarre pas
     * ("No default constructor found"). Avec un seul constructeur, l'annotation serait facultative.
     */
    @Autowired
    public JwtService(JwtEncoder encoder, SecutrackProperties proprietes) {
        this(encoder, proprietes.jwt().dureeMinutes());
    }

    /** Constructeur utilisé par les tests unitaires (visibilité package, sans Spring). */
    JwtService(JwtEncoder encoder, long dureeMinutes) {
        this.encoder = encoder;
        this.dureeMinutes = dureeMinutes;
    }

    public JetonGenere generer(Utilisateur utilisateur) {
        // Un JWT stocke les dates en secondes : on tronque pour que expiresAt corresponde exactement au jeton
        Instant maintenant = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = maintenant.plus(dureeMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMETTEUR)
                .issuedAt(maintenant)
                .expiresAt(expiration)
                .subject(utilisateur.getCourriel())
                .claim("roles", List.of(utilisateur.getRole().name()))
                .claim("nom", utilisateur.getNomComplet())
                .build();
        JwsHeader entete = JwsHeader.with(MacAlgorithm.HS256).build();

        // encode() signe le jeton avec la clé secrète de JwtConfig et renvoie la chaîne "xxx.yyy.zzz"
        String valeur = encoder.encode(JwtEncoderParameters.from(entete, claims)).getTokenValue();
        return new JetonGenere(valeur, expiration);
    }
}