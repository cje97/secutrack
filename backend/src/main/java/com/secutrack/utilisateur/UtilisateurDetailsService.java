package com.secutrack.utilisateur;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Pont entre notre table "utilisateurs" et Spring Security.
 *
 * Au login, l'AuthenticationManager appelle loadUserByUsername(courriel), récupère le hash
 * BCrypt stocké et le compare au mot de passe saisi avec le PasswordEncoder.
 * On n'écrit donc jamais nous-mêmes la comparaison des mots de passe.
 */
// @Service : bean Spring de la couche métier. Comme cette classe implémente UserDetailsService,
// Spring Security la détecte et l'utilise automatiquement pour l'authentification.
@Service
public class UtilisateurDetailsService implements UserDetailsService {

    private final UtilisateurRepository repository;

    // Injection par constructeur : Spring fournit automatiquement le repository.
    // Avec un seul constructeur, l'annotation @Autowired est inutile.
    public UtilisateurDetailsService(UtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String courriel) {
        Utilisateur u = repository.findByCourriel(courriel.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur inconnu"));
        // UserDetails : l'objet "utilisateur" que Spring Security sait manipuler
        return User.withUsername(u.getCourriel())
                .password(u.getMotDePasse())        // le hash BCrypt
                .roles(u.getRole().name())           // devient l'autorité "ROLE_ADMIN" ou "ROLE_USER"
                .build();
    }
}
