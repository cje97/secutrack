package com.secutrack.ticket;

import com.secutrack.ticket.dto.AssignationRequest;
import com.secutrack.ticket.dto.FiltreTickets;
import com.secutrack.ticket.dto.StatistiquesResponse;
import com.secutrack.ticket.dto.StatutRequest;
import com.secutrack.ticket.dto.TicketRequest;
import com.secutrack.ticket.dto.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * API REST des tickets.
 *
 * | Verbe  | URL                              | Rôle                                  | Code   |
 * |--------|----------------------------------|---------------------------------------|--------|
 * | GET    | /api/tickets                     | recherche paginée                     | 200    |
 * | GET    | /api/tickets/statistiques        | compteurs du tableau de bord          | 200    |
 * | GET    | /api/tickets/{id}                | détail                                | 200    |
 * | POST   | /api/tickets                     | création                              | 201    |
 * | PUT    | /api/tickets/{id}                | modification complète                 | 200    |
 * | PATCH  | /api/tickets/{id}/statut         | modification partielle : statut       | 200    |
 * | PATCH  | /api/tickets/{id}/assignation    | modification partielle : assigné      | 200    |
 * | DELETE | /api/tickets/{id}                | suppression (ADMIN)                   | 204    |
 */
@Tag(name = "Tickets", description = "Gestion des tickets de sécurité")
// @RestController : les méthodes renvoient des objets convertis en JSON par Jackson
@RestController
// @RequestMapping : toutes les URL de cette classe commencent par /api/tickets
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @Operation(summary = "Rechercher les tickets (filtres, pagination et tri)",
               description = "Exemple : ?statut=OUVERT&severite=CRITIQUE&recherche=vpn&page=0&size=10&sort=creeLe,desc")
    // @GetMapping sans chemin : répond à GET /api/tickets
    @GetMapping
    public Page<TicketResponse> rechercher(
            // @ParameterObject (springdoc) : affiche chaque champ du filtre comme un paramètre dans Swagger
            @ParameterObject FiltreTickets filtre,
            // Pageable : Spring le construit depuis ?page=0&size=10&sort=creeLe,desc
            // @PageableDefault : valeurs utilisées si le client n'envoie rien
            @ParameterObject @PageableDefault(size = 10, sort = "creeLe", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.rechercher(filtre, pageable);
    }

    @Operation(summary = "Statistiques pour le tableau de bord")
    // "/statistiques" est un chemin fixe : Spring le préfère à "/{id}", il n'y a donc pas de conflit
    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques(@AuthenticationPrincipal Jwt jwt) {
        return service.statistiques(jwt.getSubject());
    }

    @Operation(summary = "Détail d'un ticket")
    // @PathVariable : lit {id} dans l'URL (/api/tickets/42 → id = 42)
    @GetMapping("/{id}")
    public TicketResponse trouver(@PathVariable Long id) {
        return service.trouver(id);
    }

    @Operation(summary = "Créer un ticket (statut initial : OUVERT)")
    @PostMapping
    public ResponseEntity<TicketResponse> creer(
            // @Valid : vérifie les contraintes de TicketRequest (@NotBlank, @Size...) ; échec → 400
            // @RequestBody : convertit le JSON reçu en TicketRequest
            @Valid @RequestBody TicketRequest request,
            // @AuthenticationPrincipal : le JWT de l'utilisateur connecté ; getSubject() = son courriel
            @AuthenticationPrincipal Jwt jwt) {
        TicketResponse cree = service.creer(request, jwt.getSubject());
        // Bonne pratique REST : 201 Created + header "Location: /api/tickets/{id}" de la nouvelle ressource
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(cree.id()).toUri();
        // ResponseEntity : contrôle total sur le statut HTTP, les headers et le corps de la réponse
        return ResponseEntity.created(location).body(cree);
    }

    @Operation(summary = "Modifier un ticket (créateur ou ADMIN)")
    // @PutMapping : remplacement complet des champs modifiables
    @PutMapping("/{id}")
    public TicketResponse modifier(@PathVariable Long id, @Valid @RequestBody TicketRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.modifier(id, request, jwt.getSubject());
    }

    @Operation(summary = "Changer le statut (RESOLU et FERME : ADMIN seulement)")
    // @PatchMapping : modification partielle (un seul champ)
    @PatchMapping("/{id}/statut")
    public TicketResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return service.changerStatut(id, request.statut(), jwt.getSubject());
    }

    @Operation(summary = "Assigner ou désassigner un ticket (ADMIN)")
    @PatchMapping("/{id}/assignation")
    // @PreAuthorize : vérifié AVANT d'exécuter la méthode ; un USER reçoit 403
    // (actif grâce à @EnableMethodSecurity dans SecuriteConfig)
    @PreAuthorize("hasRole('ADMIN')")
    public TicketResponse assigner(@PathVariable Long id, @Valid @RequestBody AssignationRequest request) {
        return service.assigner(id, request.assigneId());
    }

    @Operation(summary = "Supprimer un ticket (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    // @ResponseStatus(NO_CONTENT) : 204 = succès sans corps de réponse
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
