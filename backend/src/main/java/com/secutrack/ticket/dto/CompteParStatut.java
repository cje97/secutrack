package com.secutrack.ticket.dto;

import com.secutrack.ticket.Statut;

/**
 * Projection : résultat d'une requête d'agrégation (GROUP BY), pas une entité.
 * Construit directement par la requête JPQL "SELECT new ...CompteParStatut(t.statut, COUNT(t))".
 */
public record CompteParStatut(Statut statut, Long nombre) {
}
