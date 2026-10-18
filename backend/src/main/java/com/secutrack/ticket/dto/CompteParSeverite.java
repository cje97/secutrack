package com.secutrack.ticket.dto;

import com.secutrack.ticket.Severite;

/** Projection : nombre de tickets par sévérité (voir TicketRepository.compterParSeverite). */
public record CompteParSeverite(Severite severite, Long nombre) {
}
