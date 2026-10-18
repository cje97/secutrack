package com.secutrack.auth.dto;

import com.secutrack.utilisateur.UtilisateurDto;

import java.time.Instant;

/**
 * Réponse du login et de l'inscription.
 *
 * token       : le JWT, qu'Angular renverra dans le header "Authorization: Bearer ..."
 * expiresAt   : date d'expiration, pour qu'Angular sache quand la session se termine
 * utilisateur : le profil, pour afficher le nom et adapter l'interface au rôle
 */
public record AuthResponse(String token, Instant expiresAt, UtilisateurDto utilisateur) {
}
