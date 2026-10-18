package com.secutrack.ticket;

import com.secutrack.common.BusinessException;
import com.secutrack.common.RessourceIntrouvableException;
import com.secutrack.ticket.dto.CompteParSeverite;
import com.secutrack.ticket.dto.CompteParStatut;
import com.secutrack.ticket.dto.FiltreTickets;
import com.secutrack.ticket.dto.StatistiquesResponse;
import com.secutrack.ticket.dto.TicketRequest;
import com.secutrack.ticket.dto.TicketResponse;
import com.secutrack.utilisateur.Utilisateur;
import com.secutrack.utilisateur.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * Cœur métier des tickets. Toutes les règles de gestion sont ici (jamais dans le contrôleur) :
 * <ul>
 *   <li>tout utilisateur connecté peut consulter et créer des tickets ;</li>
 *   <li>le créateur ou un ADMIN peut modifier le contenu ;</li>
 *   <li>le créateur, l'assigné ou un ADMIN peut changer le statut,
 *       mais seul un ADMIN peut passer à RESOLU ou FERME ;</li>
 *   <li>un ticket FERME n'est plus modifiable ;</li>
 *   <li>seul un ADMIN peut assigner ou supprimer (aussi vérifié par @PreAuthorize dans le contrôleur).</li>
 * </ul>
 * Le frontend masque les boutons interdits, mais c'est ICI que la sécurité est réellement garantie :
 * un utilisateur peut toujours appeler l'API directement (curl, Postman).
 */
// @Service : bean de la couche métier
@Service
// @Transactional(readOnly = true) : par défaut, chaque méthode publique s'exécute dans une transaction
// en lecture seule. Les méthodes qui écrivent la surchargent avec @Transactional tout court.
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository tickets;
    private final UtilisateurRepository utilisateurs;
    private final TicketMapper mapper;

    // Injection par constructeur : Spring fournit les trois beans automatiquement
    public TicketService(TicketRepository tickets, UtilisateurRepository utilisateurs, TicketMapper mapper) {
        this.tickets = tickets;
        this.utilisateurs = utilisateurs;
        this.mapper = mapper;
    }

    /** Pageable contient le numéro de page, la taille et le tri, construits par Spring depuis l'URL. */
    public Page<TicketResponse> rechercher(FiltreTickets filtre, Pageable pageable) {
        // page.map() convertit chaque Ticket de la page en TicketResponse, en gardant les infos de pagination
        return tickets.findAll(TicketSpecifications.avecFiltre(filtre), pageable).map(mapper::toResponse);
    }

    public TicketResponse trouver(Long id) {
        return mapper.toResponse(charger(id));
    }

    // @Transactional : écriture. Si une exception non vérifiée survient, tout est annulé (rollback).
    @Transactional
    public TicketResponse creer(TicketRequest request, String courrielCourant) {
        Utilisateur createur = utilisateur(courrielCourant);   // le créateur vient du JWT, pas du client
        Ticket ticket = new Ticket(request.titre().trim(), request.description(), request.type(),
                request.severite(), createur);
        return mapper.toResponse(tickets.save(ticket));       // save() → INSERT
    }

    @Transactional
    public TicketResponse modifier(Long id, TicketRequest request, String courrielCourant) {
        Ticket ticket = charger(id);
        Utilisateur courant = utilisateur(courrielCourant);
        verifierModifiable(ticket);
        if (!courant.estAdmin() && !ticket.estCreePar(courant)) {
            // AccessDeniedException → 403 (GlobalExceptionHandler)
            throw new AccessDeniedException("Seul le créateur du ticket ou un administrateur peut le modifier");
        }
        ticket.modifier(request.titre().trim(), request.description(), request.type(), request.severite());
        // Pas de save() : le ticket chargé est "managé" par Hibernate. À la fin de la transaction,
        // Hibernate compare son état avec celui du chargement et génère l'UPDATE (dirty checking).
        return mapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse changerStatut(Long id, Statut nouveauStatut, String courrielCourant) {
        Ticket ticket = charger(id);
        Utilisateur courant = utilisateur(courrielCourant);
        verifierModifiable(ticket);
        if (Statut.RESERVES_ADMIN.contains(nouveauStatut) && !courant.estAdmin()) {
            throw new AccessDeniedException("Seul un administrateur peut passer un ticket à " + nouveauStatut);
        }
        if (!courant.estAdmin() && !ticket.estCreePar(courant) && !ticket.estAssigneA(courant)) {
            throw new AccessDeniedException("Vous n'êtes ni le créateur ni l'assigné de ce ticket");
        }
        ticket.setStatut(nouveauStatut);
        return mapper.toResponse(ticket);
    }

    /** assigneId null = retirer l'assignation. Le contrôle "ADMIN seulement" est fait par @PreAuthorize. */
    @Transactional
    public TicketResponse assigner(Long id, Long assigneId) {
        Ticket ticket = charger(id);
        verifierModifiable(ticket);
        Utilisateur assigne = (assigneId == null) ? null : utilisateurs.findById(assigneId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur " + assigneId + " introuvable"));
        ticket.setAssigne(assigne);
        return mapper.toResponse(ticket);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!tickets.existsById(id)) {
            throw new RessourceIntrouvableException("Ticket " + id + " introuvable");   // → 404
        }
        tickets.deleteById(id);
    }

    /** Compteurs du tableau de bord. EnumMap : on initialise chaque valeur à 0 pour qu'Angular les reçoive toutes. */
    public StatistiquesResponse statistiques(String courrielCourant) {
        Map<Statut, Long> parStatut = new EnumMap<>(Statut.class);
        for (Statut s : Statut.values()) {
            parStatut.put(s, 0L);
        }
        for (CompteParStatut c : tickets.compterParStatut()) {
            parStatut.put(c.statut(), c.nombre());
        }

        Map<Severite, Long> parSeverite = new EnumMap<>(Severite.class);
        for (Severite s : Severite.values()) {
            parSeverite.put(s, 0L);
        }
        for (CompteParSeverite c : tickets.compterParSeverite(Statut.TERMINES)) {
            parSeverite.put(c.severite(), c.nombre());
        }

        long total = parStatut.values().stream().mapToLong(Long::longValue).sum();
        long assignesAMoi = tickets.countByAssigneCourrielAndStatutNotIn(courrielCourant, Statut.TERMINES);
        return new StatistiquesResponse(total, parStatut, parSeverite, assignesAMoi);
    }

    // ---- Méthodes privées utilitaires ----

    private void verifierModifiable(Ticket ticket) {
        if (ticket.estFerme()) {
            throw new BusinessException("Un ticket fermé ne peut plus être modifié");   // → 409
        }
    }

    /** findById renvoie un Optional : orElseThrow lève notre exception (→ 404) s'il est vide. */
    private Ticket charger(Long id) {
        return tickets.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Ticket " + id + " introuvable"));
    }

    private Utilisateur utilisateur(String courriel) {
        return utilisateurs.findByCourriel(courriel)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }
}
