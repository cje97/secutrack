package com.secutrack.ticket;

import com.secutrack.utilisateur.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * Entité centrale de l'application : un ticket de sécurité (table "tickets").
 *
 * Relations avec Utilisateur :
 * <pre>
 *  utilisateurs                 tickets
 *  ┌──────────┐   1      N   ┌───────────────────┐
 *  │ id (PK)  │◀──────────── │ createur_id (FK)  │  obligatoire
 *  │          │◀──────────── │ assigne_id  (FK)  │  optionnel
 *  └──────────┘              └───────────────────┘
 * </pre>
 */
// @Entity : classe persistée en base (une instance = une ligne de la table)
@Entity
// @Table : nom de la table + index. Un index accélère les recherches sur une colonne
// (ici les filtres par statut et par sévérité du tableau de bord).
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_statut", columnList = "statut"),
        @Index(name = "idx_ticket_severite", columnList = "severite")
})
public class Ticket {

    // @Id + @GeneratedValue(IDENTITY) : clé primaire auto-incrémentée par la base
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false, length = 150) → colonne VARCHAR(150) NOT NULL
    // La même limite est vérifiée dans TicketRequest (@Size(max = 150)) pour renvoyer un 400 clair.
    @Column(nullable = false, length = 150)
    private String titre;

    @Column(length = 4000)
    private String description;

    // @Enumerated(STRING) : stocke "INCIDENT" en texte, jamais la position 0, 1, 2 (EnumType.ORDINAL)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeTicket type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severite severite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut;

    // @ManyToOne : plusieurs tickets → un utilisateur. C'est le côté qui porte la clé étrangère.
    //   fetch = LAZY : l'utilisateur n'est chargé que si on y accède (par défaut, @ManyToOne est EAGER :
    //                  il chargerait toujours l'utilisateur, même inutilement).
    //   optional = false : un ticket a toujours un créateur.
    // @JoinColumn : nom de la colonne de clé étrangère dans la table tickets.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "createur_id", nullable = false)
    private Utilisateur createur;

    // Assigné optionnel : la colonne peut être NULL (ticket pas encore pris en charge)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_id")
    private Utilisateur assigne;

    // Instant : un point précis dans le temps (UTC), sans ambiguïté de fuseau horaire
    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    private Instant misAJourLe;

    // @Version : verrouillage optimiste. Hibernate incrémente ce compteur à chaque UPDATE et vérifie
    // qu'il n'a pas changé entre la lecture et l'écriture. Si deux personnes modifient le même ticket
    // en même temps, la seconde reçoit une erreur (409) au lieu d'écraser le travail de la première.
    @Version
    private Long version;

    /** Constructeur vide obligatoire pour JPA (Hibernate l'utilise pour recréer l'objet depuis la base). */
    protected Ticket() {
    }

    public Ticket(String titre, String description, TypeTicket type, Severite severite, Utilisateur createur) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.severite = severite;
        this.createur = createur;
        this.statut = Statut.OUVERT;     // règle métier : tout ticket naît OUVERT
    }

    // @PrePersist : appelée automatiquement juste avant l'INSERT
    @PrePersist
    void avantCreation() {
        this.creeLe = Instant.now();
        this.misAJourLe = this.creeLe;
    }

    // @PreUpdate : appelée automatiquement juste avant chaque UPDATE
    @PreUpdate
    void avantMiseAJour() {
        this.misAJourLe = Instant.now();
    }

    // ---- Comportements métier ----
    // Mettre ces petites méthodes dans l'entité rend le service plus lisible : ticket.estFerme()

    public void modifier(String titre, String description, TypeTicket type, Severite severite) {
        this.titre = titre;
        this.description = description;
        this.type = type;
        this.severite = severite;
    }

    public boolean estFerme() {
        return statut == Statut.FERME;
    }

    /** Comparaison par courriel (unique) : fonctionne même avant que l'id soit généré (tests unitaires). */
    public boolean estCreePar(Utilisateur u) {
        return createur.getCourriel().equals(u.getCourriel());
    }

    public boolean estAssigneA(Utilisateur u) {
        return assigne != null && assigne.getCourriel().equals(u.getCourriel());
    }

    // ---- Accesseurs ----

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public TypeTicket getType() { return type; }
    public Severite getSeverite() { return severite; }
    public Statut getStatut() { return statut; }
    public Utilisateur getCreateur() { return createur; }
    public Utilisateur getAssigne() { return assigne; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getMisAJourLe() { return misAJourLe; }
    public Long getVersion() { return version; }

    public void setStatut(Statut statut) { this.statut = statut; }
    public void setAssigne(Utilisateur assigne) { this.assigne = assigne; }
}
