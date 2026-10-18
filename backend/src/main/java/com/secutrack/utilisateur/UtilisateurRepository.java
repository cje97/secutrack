package com.secutrack.utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Accès aux données des utilisateurs.
 *
 * On ne code AUCUNE implémentation : Spring Data JPA génère la classe au démarrage.
 * JpaRepository<Utilisateur, Long> = entité gérée + type de son id. On hérite gratuitement de :
 * save(), findById(), findAll(), existsById(), count(), deleteById()...
 *
 * Les méthodes ci-dessous sont des "requêtes dérivées" : Spring lit le NOM de la méthode
 * et génère le SQL correspondant.
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // → SELECT * FROM utilisateurs WHERE courriel = ?
    // Optional : oblige l'appelant à gérer le cas "pas trouvé" (orElseThrow...)
    Optional<Utilisateur> findByCourriel(String courriel);

    // → SELECT COUNT(*) > 0 FROM utilisateurs WHERE courriel = ?
    boolean existsByCourriel(String courriel);

    // → SELECT * FROM utilisateurs ORDER BY nom_complet ASC
    List<Utilisateur> findAllByOrderByNomCompletAsc();
}
