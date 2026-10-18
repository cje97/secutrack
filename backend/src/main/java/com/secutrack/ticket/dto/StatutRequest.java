package com.secutrack.ticket.dto;

import com.secutrack.ticket.Statut;
import jakarta.validation.constraints.NotNull;

/** Corps de PATCH /api/tickets/{id}/statut : { "statut": "EN_COURS" } */
public record StatutRequest(@NotNull(message = "Le statut est obligatoire") Statut statut) {
}
