package com.secutrack.demo;

import com.secutrack.config.SecutrackProperties;
import com.secutrack.ticket.Severite;
import com.secutrack.ticket.Statut;
import com.secutrack.ticket.Ticket;
import com.secutrack.ticket.TicketRepository;
import com.secutrack.ticket.TypeTicket;
import com.secutrack.utilisateur.Role;
import com.secutrack.utilisateur.Utilisateur;
import com.secutrack.utilisateur.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Comptes et tickets de démonstration, créés au démarrage si la base est vide.
 * Désactivable avec secutrack.demo.actif=false (variable d'environnement DEMO_ACTIF).
 *
 * CommandLineRunner : interface dont la méthode run() est exécutée par Spring Boot
 * une fois l'application complètement démarrée.
 */
// @Component : bean Spring générique
@Component
// @ConditionalOnProperty : ce bean n'est créé QUE si secutrack.demo.actif=true.
// Dans les tests d'intégration, on met la propriété à false pour partir d'une base vide.
@ConditionalOnProperty(prefix = "secutrack.demo", name = "actif", havingValue = "true")
public class DonneesDemo implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DonneesDemo.class);

    private final UtilisateurRepository utilisateurs;
    private final TicketRepository tickets;
    private final PasswordEncoder passwordEncoder;
    private final SecutrackProperties proprietes;

    public DonneesDemo(UtilisateurRepository utilisateurs, TicketRepository tickets,
                       PasswordEncoder passwordEncoder, SecutrackProperties proprietes) {
        this.utilisateurs = utilisateurs;
        this.tickets = tickets;
        this.passwordEncoder = passwordEncoder;
        this.proprietes = proprietes;
    }

    // @Transactional : toutes les insertions dans une seule transaction (tout ou rien)
    @Override
    @Transactional
    public void run(String... args) {
        if (utilisateurs.count() > 0) {
            return;   // base déjà remplie (ex. : PostgreSQL avec un volume Docker) : on ne duplique rien
        }
        String mdp = passwordEncoder.encode(proprietes.demo().motDePasse());
        Utilisateur admin = utilisateurs.save(new Utilisateur("Alice Admin", "admin@secutrack.local", mdp, Role.ADMIN));
        Utilisateur analyste = utilisateurs.save(new Utilisateur("Bruno Analyste", "analyste@secutrack.local", mdp, Role.USER));
        Utilisateur dev = utilisateurs.save(new Utilisateur("Chloé Développeuse", "dev@secutrack.local", mdp, Role.USER));

        Ticket phishing = new Ticket("Campagne d'hameçonnage ciblant la paie",
                "Plusieurs employés ont reçu un courriel imitant le portail de paie avec un lien malveillant.",
                TypeTicket.INCIDENT, Severite.CRITIQUE, analyste);
        phishing.setAssigne(analyste);
        phishing.setStatut(Statut.EN_COURS);

        Ticket log4j = new Ticket("Bibliothèque vulnérable détectée dans un service interne",
                "Le scan de dépendances signale une version vulnérable d'une bibliothèque de journalisation.",
                TypeTicket.VULNERABILITE, Severite.ELEVEE, dev);
        log4j.setAssigne(dev);

        Ticket acces = new Ticket("Accès VPN pour un consultant externe",
                "Demande d'accès temporaire de 30 jours au VPN pour le projet de migration.",
                TypeTicket.DEMANDE_ACCES, Severite.FAIBLE, dev);

        Ticket mdpFaible = new Ticket("Politique de mots de passe trop permissive sur l'outil de rapports",
                "Les mots de passe de 6 caractères sont acceptés.", TypeTicket.VULNERABILITE, Severite.MOYENNE, analyste);
        mdpFaible.setStatut(Statut.RESOLU);

        Ticket usb = new Ticket("Clé USB inconnue trouvée en salle de réunion",
                "Remise à l'équipe de sécurité pour analyse.", TypeTicket.INCIDENT, Severite.MOYENNE, admin);
        usb.setStatut(Statut.FERME);

        tickets.saveAll(List.of(phishing, log4j, acces, mdpFaible, usb));
        log.info("Données de démonstration créées : admin@ / analyste@ / dev@secutrack.local");
    }
}
