package com.secutrack.ticket;

import com.secutrack.ticket.dto.TicketResponse;
import com.secutrack.utilisateur.UtilisateurResume;
import org.springframework.stereotype.Component;

/**
 * Conversion entité Ticket → DTO TicketResponse.
 *
 * Toujours appelé dans le service, donc à l'intérieur d'une transaction : l'accès aux relations
 * LAZY (getCreateur(), getAssigne()) fonctionne. Appelé depuis le contrôleur, hors transaction,
 * il pourrait lever une LazyInitializationException.
 *
 * Sur de gros projets, on génère ce code avec la bibliothèque MapStruct.
 */
// @Component : bean Spring générique (ni service métier, ni repository, ni contrôleur)
@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getTitre(),
                t.getDescription(),
                t.getType(),
                t.getSeverite(),
                t.getStatut(),
                UtilisateurResume.from(t.getCreateur()),
                UtilisateurResume.from(t.getAssigne()),
                t.getCreeLe(),
                t.getMisAJourLe());
    }
}
