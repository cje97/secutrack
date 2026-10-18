package com.secutrack.auth;

import java.time.Instant;

/** Résultat de JwtService.generer() : la chaîne du jeton et sa date d'expiration. */
public record JetonGenere(String valeur, Instant expiration) {
}
