package com.secutrack.utilisateur;

/**
 * Version courte d'un utilisateur, imbriquée dans les tickets (créateur, assigné).
 * Inutile d'envoyer le rôle et la date d'inscription à chaque ticket.
 */
public record UtilisateurResume(Long id, String nomComplet, String courriel) {

    /** Renvoie null si l'utilisateur est null (ticket non assigné). */
    public static UtilisateurResume from(Utilisateur u) {
        return u == null ? null : new UtilisateurResume(u.getId(), u.getNomComplet(), u.getCourriel());
    }
}
