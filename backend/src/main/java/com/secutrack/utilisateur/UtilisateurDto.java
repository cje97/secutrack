package com.secutrack.utilisateur;

import java.time.Instant;

/**
 * DTO (Data Transfer Object) : ce que l'API renvoie au client pour un utilisateur.
 *
 * On ne renvoie JAMAIS l'entité Utilisateur directement : elle contient le hash du mot de passe.
 * Le DTO choisit précisément les champs exposés.
 *
 * "record" (Java 16+) : classe immuable dont Java génère le constructeur, les accesseurs
 * (id(), nomComplet()...), equals, hashCode et toString. Idéal pour un DTO.
 */
public record UtilisateurDto(Long id, String nomComplet, String courriel, Role role, Instant creeLe) {

    /** Conversion entité → DTO (méthode "fabrique" statique). */
    public static UtilisateurDto from(Utilisateur u) {
        return new UtilisateurDto(u.getId(), u.getNomComplet(), u.getCourriel(), u.getRole(), u.getCreeLe());
    }
}
