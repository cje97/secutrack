package com.secutrack.ticket;

import com.secutrack.common.BusinessException;
import com.secutrack.ticket.dto.TicketRequest;
import com.secutrack.ticket.dto.TicketResponse;
import com.secutrack.utilisateur.Role;
import com.secutrack.utilisateur.Utilisateur;
import com.secutrack.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests UNITAIRES du service : aucun Spring, aucune base de données. Très rapides.
 * Les repositories sont remplacés par des "mocks" (faux objets) dont on programme les réponses.
 */
// @ExtendWith(MockitoExtension.class) : active Mockito dans JUnit 5 (gère @Mock, @Spy, @InjectMocks)
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    // @Mock : faux objet. Ses méthodes renvoient null/vide, sauf si on les programme avec when(...)
    @Mock TicketRepository tickets;
    @Mock UtilisateurRepository utilisateurs;
    @Spy TicketMapper mapper = new TicketMapper();     // vraie classe : on veut tester le DTO produit
    // @Spy sur un vrai objet : on utilise le vrai mapper (pas de raison de le simuler)
    // @InjectMocks : crée le vrai TicketService en lui passant les @Mock et @Spy au constructeur
    @InjectMocks TicketService service;

    private final Utilisateur admin = new Utilisateur("Alice Admin", "admin@test", "h", Role.ADMIN);
    private final Utilisateur bruno = new Utilisateur("Bruno", "bruno@test", "h", Role.USER);
    private final Utilisateur chloe = new Utilisateur("Chloé", "chloe@test", "h", Role.USER);

    private Ticket ticketDeBruno() {
        return new Ticket("Phishing", "Courriel suspect", TypeTicket.INCIDENT, Severite.ELEVEE, bruno);
    }

    // @DisplayName : nom lisible affiché dans les rapports de test
    // Structure de chaque test : given (préparer) / when (agir) / then (vérifier)
    @Test
    @DisplayName("creer() associe le créateur et démarre au statut OUVERT")
    void creer() {
        // when(...).thenReturn(...) : programme la réponse du mock
        when(utilisateurs.findByCourriel("bruno@test")).thenReturn(Optional.of(bruno));
        // thenAnswer : save() renvoie le ticket qu'on lui passe (comme le ferait le vrai repository)
        when(tickets.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        TicketResponse r = service.creer(
                new TicketRequest("  Phishing  ", "desc", TypeTicket.INCIDENT, Severite.ELEVEE), "bruno@test");

        // assertThat (AssertJ) : vérifications lisibles. Le titre a bien été "trim()" par le service.
        assertThat(r.titre()).isEqualTo("Phishing");
        assertThat(r.statut()).isEqualTo(Statut.OUVERT);
        assertThat(r.createur().nomComplet()).isEqualTo("Bruno");
        assertThat(r.assigne()).isNull();
    }

    @Test
    @DisplayName("un USER ne peut pas passer un ticket à RESOLU")
    void userNePeutPasResoudre() {
        when(tickets.findById(1L)).thenReturn(Optional.of(ticketDeBruno()));
        when(utilisateurs.findByCourriel("bruno@test")).thenReturn(Optional.of(bruno));

        // assertThatThrownBy : vérifie que l'appel lève bien l'exception attendue
        assertThatThrownBy(() -> service.changerStatut(1L, Statut.RESOLU, "bruno@test"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("un ADMIN peut passer un ticket à RESOLU")
    void adminPeutResoudre() {
        when(tickets.findById(1L)).thenReturn(Optional.of(ticketDeBruno()));
        when(utilisateurs.findByCourriel("admin@test")).thenReturn(Optional.of(admin));

        TicketResponse r = service.changerStatut(1L, Statut.RESOLU, "admin@test");

        assertThat(r.statut()).isEqualTo(Statut.RESOLU);
    }

    @Test
    @DisplayName("le créateur peut passer son ticket EN_COURS")
    void createurPeutPasserEnCours() {
        when(tickets.findById(1L)).thenReturn(Optional.of(ticketDeBruno()));
        when(utilisateurs.findByCourriel("bruno@test")).thenReturn(Optional.of(bruno));

        assertThat(service.changerStatut(1L, Statut.EN_COURS, "bruno@test").statut()).isEqualTo(Statut.EN_COURS);
    }

    @Test
    @DisplayName("un USER ne peut pas modifier le ticket de quelqu'un d'autre")
    void userNePeutPasModifierTicketDautrui() {
        when(tickets.findById(1L)).thenReturn(Optional.of(ticketDeBruno()));
        when(utilisateurs.findByCourriel("chloe@test")).thenReturn(Optional.of(chloe));
        TicketRequest request = new TicketRequest("Nouveau titre", null, TypeTicket.INCIDENT, Severite.FAIBLE);

        assertThatThrownBy(() -> service.modifier(1L, request, "chloe@test"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("un ticket FERME n'est plus modifiable, même par un ADMIN")
    void ticketFermeBloque() {
        Ticket ferme = ticketDeBruno();
        ferme.setStatut(Statut.FERME);
        when(tickets.findById(1L)).thenReturn(Optional.of(ferme));
        when(utilisateurs.findByCourriel("admin@test")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.changerStatut(1L, Statut.OUVERT, "admin@test"))
                .isInstanceOf(BusinessException.class);
    }
}
