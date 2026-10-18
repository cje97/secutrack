package com.secutrack.ticket.dto;

import com.secutrack.ticket.Severite;
import com.secutrack.ticket.Statut;
import com.secutrack.ticket.TypeTicket;

/**
 * Filtres optionnels de la recherche, lus dans l'URL :
 * GET /api/tickets?statut=OUVERT&severite=CRITIQUE&type=INCIDENT&assigneId=3&recherche=vpn
 *
 * Spring remplit automatiquement ce record à partir des paramètres de la requête
 * (chaque nom de champ = nom du paramètre). Un paramètre absent vaut null = pas de filtre.
 */
public record FiltreTickets(Statut statut, Severite severite, TypeTicket type, Long assigneId, String recherche) {
}
