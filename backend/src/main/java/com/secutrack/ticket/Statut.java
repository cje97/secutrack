package com.secutrack.ticket;

import java.util.EnumSet;
import java.util.Set;

/**
 * Cycle de vie d'un ticket : OUVERT → EN_COURS → RESOLU → FERME.
 * Un ticket FERME est verrouillé (TicketService.verifierModifiable).
 */
public enum Statut {
    OUVERT, EN_COURS, RESOLU, FERME;

    /** Statuts que seul un ADMIN peut appliquer. Le frontend a la même liste (STATUTS_ADMIN). */
    public static final Set<Statut> RESERVES_ADMIN = EnumSet.of(RESOLU, FERME);

    /** Statuts considérés comme "terminés" dans les statistiques du tableau de bord. */
    public static final Set<Statut> TERMINES = EnumSet.of(RESOLU, FERME);
}
