package com.secutrack.auth;

import com.secutrack.auth.dto.AuthResponse;
import com.secutrack.auth.dto.LoginRequest;
import com.secutrack.auth.dto.RegisterRequest;
import com.secutrack.common.BusinessException;
import com.secutrack.common.RessourceIntrouvableException;
import com.secutrack.utilisateur.Role;
import com.secutrack.utilisateur.Utilisateur;
import com.secutrack.utilisateur.UtilisateurDto;
import com.secutrack.utilisateur.UtilisateurRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logique d'inscription et de connexion.
 */
// @Service : bean de la couche métier (Spring le crée une seule fois et l'injecte dans le contrôleur)
@Service
// Lecture seule par défaut ; les méthodes qui écrivent ajoutent leur propre @Transactional
@Transactional(readOnly = true)
public class AuthService {

    private final UtilisateurRepository utilisateurs;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Injection par constructeur avec des champs "final" : dépendances obligatoires et immuables,
    // et la classe se teste facilement en passant des faux objets (mocks) au constructeur.
    public AuthService(UtilisateurRepository utilisateurs, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.utilisateurs = utilisateurs;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Inscription : toujours avec le rôle USER (sinon n'importe qui pourrait s'inscrire ADMIN).
     * Seul un ADMIN peut ensuite promouvoir quelqu'un (UtilisateurService.changerRole).
     */
    // @Transactional : écriture en base ; tout est annulé si une exception survient
    @Transactional
    public AuthResponse inscrire(RegisterRequest request) {
        String courriel = normaliser(request.courriel());
        if (utilisateurs.existsByCourriel(courriel)) {
            throw new BusinessException("Ce courriel est déjà utilisé");   // → 409
        }
        // passwordEncoder.encode() : hache le mot de passe avec BCrypt avant de le stocker
        Utilisateur u = utilisateurs.save(new Utilisateur(
                request.nomComplet().trim(), courriel, passwordEncoder.encode(request.motDePasse()), Role.USER));
        return reponse(u);
    }

    /**
     * Connexion. authenticate() charge l'utilisateur via UtilisateurDetailsService et compare
     * les mots de passe avec BCrypt. S'ils ne correspondent pas, il lève BadCredentialsException,
     * que le GlobalExceptionHandler transforme en 401.
     */
    public AuthResponse connecter(LoginRequest request) {
        String courriel = normaliser(request.courriel());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(courriel, request.motDePasse()));
        return reponse(charger(courriel));
    }

    public UtilisateurDto moi(String courriel) {
        return UtilisateurDto.from(charger(courriel));
    }

    private Utilisateur charger(String courriel) {
        return utilisateurs.findByCourriel(courriel)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }

    private AuthResponse reponse(Utilisateur u) {
        JetonGenere jeton = jwtService.generer(u);
        return new AuthResponse(jeton.valeur(), jeton.expiration(), UtilisateurDto.from(u));
    }

    /** " Admin@Secutrack.LOCAL " et "admin@secutrack.local" désignent le même compte. */
    private static String normaliser(String courriel) {
        return courriel.trim().toLowerCase();
    }
}
