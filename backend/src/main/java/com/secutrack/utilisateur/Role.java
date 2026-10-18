package com.secutrack.utilisateur;

/**
 * Rôles de l'application.
 * Dans le JWT, ils deviennent la claim "roles": ["ADMIN"], puis l'autorité Spring "ROLE_ADMIN".
 */
public enum Role {
    /** Peut créer des tickets, modifier les siens, les passer en cours. */
    USER,
    /** Peut en plus résoudre, fermer, assigner, supprimer et gérer les rôles. */
    ADMIN
}
