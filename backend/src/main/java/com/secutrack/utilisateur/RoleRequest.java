package com.secutrack.utilisateur;

import jakarta.validation.constraints.NotNull;

/**
 * Corps de PATCH /api/utilisateurs/{id}/role : { "role": "ADMIN" }
 *
 * @NotNull : Bean Validation refuse la requête (400) si "role" est absent ou null.
 * La validation n'est déclenchée que si le contrôleur met @Valid devant @RequestBody.
 */
public record RoleRequest(@NotNull(message = "Le rôle est obligatoire") Role role) {
}
