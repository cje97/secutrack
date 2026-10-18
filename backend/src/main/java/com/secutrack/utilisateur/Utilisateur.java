package com.secutrack.utilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entité JPA : un objet Java qui correspond à une ligne de la table "utilisateurs".
 * Hibernate (l'implémentation de JPA utilisée par Spring Boot) génère le SQL tout seul :
 * on manipule des objets, pas des requêtes INSERT / UPDATE.
 */
// @Entity : cette classe est persistée en base de données (une instance = une ligne)
@Entity
// @Table : nom de la table. Sans elle, la table s'appellerait "utilisateur".
// On évite "user", qui est un mot réservé en PostgreSQL (erreur SQL déroutante garantie).
@Table(name = "utilisateurs")
public class Utilisateur {

    // @Id : clé primaire de la table
    @Id
    // @GeneratedValue(IDENTITY) : l'id est généré par la base (auto-incrément) au moment de l'INSERT
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column : configure la colonne. nullable = false → NOT NULL en base ; length = 100 → VARCHAR(100)
    @Column(nullable = false, length = 100)
    private String nomComplet;

    // unique = true → contrainte d'unicité en base : deux comptes ne peuvent pas avoir le même courriel
    @Column(nullable = false, unique = true, length = 255)
    private String courriel;

    /** Hash BCrypt (60 caractères), jamais le mot de passe en clair. */
    @Column(nullable = false, length = 100)
    private String motDePasse;

    // @Enumerated(EnumType.STRING) : stocke le texte "ADMIN" plutôt que la position (0, 1).
    // Avec ORDINAL (le défaut !), ajouter une valeur au début de l'enum fausserait toutes les données.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // updatable = false : Hibernate n'inclut jamais cette colonne dans un UPDATE
    @Column(nullable = false, updatable = false)
    private Instant creeLe;

    /**
     * Constructeur sans argument OBLIGATOIRE pour JPA : Hibernate crée l'objet vide
     * puis remplit les champs quand il lit une ligne en base.
     * "protected" pour qu'on ne l'utilise pas par erreur dans notre code.
     */
    protected Utilisateur() {
    }

    /** Constructeur utilisé par notre code (inscription, données de démo). */
    public Utilisateur(String nomComplet, String courriel, String motDePasseHache, Role role) {
        this.nomComplet = nomComplet;
        this.courriel = courriel;
        this.motDePasse = motDePasseHache;
        this.role = role;
    }

    // @PrePersist : méthode appelée automatiquement par Hibernate juste AVANT l'INSERT
    @PrePersist
    void avantCreation() {
        this.creeLe = Instant.now();
    }

    /** Petite méthode métier : rend le code des services plus lisible (u.estAdmin()). */
    public boolean estAdmin() {
        return role == Role.ADMIN;
    }

    // Getters : Hibernate n'en a pas besoin, mais notre code et les mappers DTO, oui.
    // Pas de setter pour courriel ou motDePasse : ils ne doivent pas changer par erreur.
    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public String getCourriel() { return courriel; }
    public String getMotDePasse() { return motDePasse; }
    public Role getRole() { return role; }
    public Instant getCreeLe() { return creeLe; }

    public void setRole(Role role) { this.role = role; }
}
