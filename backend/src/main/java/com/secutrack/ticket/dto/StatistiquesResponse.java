package com.secutrack.ticket.dto;

import com.secutrack.ticket.Severite;
import com.secutrack.ticket.Statut;

import java.util.Map;

/**
 * Données des cartes du tableau de bord (GET /api/tickets/statistiques).
 * Exemple JSON :
 * <pre>
 * { "total": 5,
 *   "parStatut": { "OUVERT": 2, "EN_COURS": 1, "RESOLU": 1, "FERME": 1 },
 *   "actifsParSeverite": { "FAIBLE": 1, "MOYENNE": 0, "ELEVEE": 1, "CRITIQUE": 1 },
 *   "assignesAMoi": 1 }
 * </pre>
 * actifsParSeverite ne compte que les tickets non résolus et non fermés.
 */
public record StatistiquesResponse(
        long total,
        Map<Statut, Long> parStatut,
        Map<Severite, Long> actifsParSeverite,
        long assignesAMoi) {
}
