package com.secutrack.ticket;

import com.secutrack.ticket.dto.FiltreTickets;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Construit dynamiquement le WHERE de la recherche : seuls les filtres renseignés sont appliqués.
 *
 * Une Specification est une condition réutilisable : (root, query, cb) -> condition
 *   - root  : l'entité interrogée (Ticket) ; root.get("statut") = la colonne statut
 *   - query : la requête en cours (rarement utilisée)
 *   - cb    : le CriteriaBuilder, qui fabrique les conditions (equal, like, or, lower...)
 *
 * C'est l'équivalent typé et sûr d'un "WHERE 1=1 AND ..." concaténé à la main
 * (et sans risque d'injection SQL : les valeurs sont toujours passées en paramètres).
 */
public final class TicketSpecifications {

    private TicketSpecifications() {
        // classe utilitaire : pas d'instance
    }

    public static Specification<Ticket> avecFiltre(FiltreTickets filtre) {
        List<Specification<Ticket>> conditions = new ArrayList<>();

        if (filtre.statut() != null) {
            // → WHERE statut = ?
            conditions.add((root, query, cb) -> cb.equal(root.get("statut"), filtre.statut()));
        }
        if (filtre.severite() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("severite"), filtre.severite()));
        }
        if (filtre.type() != null) {
            conditions.add((root, query, cb) -> cb.equal(root.get("type"), filtre.type()));
        }
        if (filtre.assigneId() != null) {
            // root.get("assigne").get("id") : on suit la relation @ManyToOne → assigne_id = ?
            conditions.add((root, query, cb) -> cb.equal(root.get("assigne").get("id"), filtre.assigneId()));
        }
        if (StringUtils.hasText(filtre.recherche())) {
            // Recherche insensible à la casse dans le titre OU la description :
            // → WHERE LOWER(titre) LIKE '%vpn%' OR LOWER(description) LIKE '%vpn%'
            String motif = "%" + filtre.recherche().trim().toLowerCase() + "%";
            conditions.add((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.<String>get("titre")), motif),
                    cb.like(cb.lower(root.<String>get("description")), motif)));
        }
        // allOf : combine toutes les conditions avec AND (aucune condition = tous les tickets)
        return Specification.allOf(conditions);
    }
}
