package com.secutrack.utilisateur;

import com.secutrack.common.BusinessException;
import com.secutrack.common.RessourceIntrouvableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logique métier de l'administration des utilisateurs.
 */
@Service
// @Transactional(readOnly = true) sur la classe : chaque méthode s'exécute dans une transaction
// en lecture seule par défaut (Hibernate ne surveille pas les modifications : plus rapide).
@Transactional(readOnly = true)
public class UtilisateurService {

    private final UtilisateurRepository repository;

    public UtilisateurService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    public List<UtilisateurDto> lister() {
        return repository.findAllByOrderByNomCompletAsc().stream().map(UtilisateurDto::from).toList();
    }

    /**
     * Change le rôle d'un utilisateur.
     * Règle : un administrateur ne peut pas retirer son propre rôle ADMIN
     * (sinon il pourrait se retrouver sans aucun administrateur et bloquer l'application).
     */
    // @Transactional (sans readOnly) : surcharge celui de la classe pour autoriser l'écriture.
    // Si une RuntimeException est levée, tout est annulé (rollback).
    @Transactional
    public UtilisateurDto changerRole(Long id, Role role, String courrielCourant) {
        Utilisateur u = repository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur " + id + " introuvable"));
        if (u.getCourriel().equals(courrielCourant) && role != Role.ADMIN) {
            throw new BusinessException("Vous ne pouvez pas retirer votre propre rôle administrateur");
        }
        // Pas besoin d'appeler repository.save() : l'entité chargée est "managée" par Hibernate,
        // qui détecte le changement et fait l'UPDATE à la fin de la transaction (dirty checking).
        u.setRole(role);
        return UtilisateurDto.from(u);
    }
}
