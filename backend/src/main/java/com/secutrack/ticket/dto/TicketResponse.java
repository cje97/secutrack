package com.secutrack.ticket.dto;

import com.secutrack.ticket.Severite;
import com.secutrack.ticket.Statut;
import com.secutrack.ticket.TypeTicket;
import com.secutrack.utilisateur.UtilisateurResume;

import java.time.Instant;

/**
 * Ce que l'API renvoie pour un ticket (DTO de sortie).
 *
 * Pourquoi ne pas renvoyer l'entité Ticket directement ?
 *   - elle contient des champs internes (version) ;
 *   - ses relations LAZY provoqueraient une LazyInitializationException pendant la conversion en JSON ;
 *   - on pourrait renommer une colonne sans casser le contrat utilisé par Angular.
 */
public record TicketResponse(
        Long id,
        String titre,
        String description,
        TypeTicket type,
        Severite severite,
        Statut statut,
        UtilisateurResume createur,
        UtilisateurResume assigne,     // null si personne n'est assigné
        Instant creeLe,
        Instant misAJourLe) {
}
