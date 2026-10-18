package com.secutrack.ticket.dto;

import jakarta.validation.constraints.Positive;

/**
 * Corps de PATCH /api/tickets/{id}/assignation : { "assigneId": 3 }
 * assigneId = null pour retirer l'assignation.
 * @Positive : si une valeur est fournie, elle doit être > 0 (null reste accepté).
 */
public record AssignationRequest(@Positive Long assigneId) {
}
