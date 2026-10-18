package com.secutrack.ticket.dto;

import com.secutrack.ticket.Severite;
import com.secutrack.ticket.TypeTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps de POST /api/tickets (création) et PUT /api/tickets/{id} (modification).
 * Le client n'envoie ni le statut, ni le créateur : le serveur les décide (sécurité).
 */
public record TicketRequest(
        // @NotBlank : obligatoire et pas seulement des espaces
        @NotBlank(message = "Le titre est obligatoire")
        // @Size(max = 150) : aligné sur @Column(length = 150) de l'entité Ticket
        @Size(max = 150, message = "150 caractères maximum")
        String titre,

        // Pas de @NotBlank : la description est optionnelle
        @Size(max = 4000, message = "4000 caractères maximum")
        String description,

        // @NotNull : pour un enum (ou un objet), "obligatoire" se dit @NotNull, pas @NotBlank
        @NotNull(message = "Le type est obligatoire")
        TypeTicket type,

        @NotNull(message = "La sévérité est obligatoire")
        Severite severite) {
}
