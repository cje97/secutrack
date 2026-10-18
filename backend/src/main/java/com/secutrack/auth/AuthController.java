package com.secutrack.auth;

import com.secutrack.auth.dto.AuthResponse;
import com.secutrack.auth.dto.LoginRequest;
import com.secutrack.auth.dto.RegisterRequest;
import com.secutrack.utilisateur.UtilisateurDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints d'authentification.
 * register et login sont publics (voir ROUTES_PUBLIQUES dans SecuriteConfig) ; /moi exige un jeton.
 *
 * Rôle d'un contrôleur : recevoir la requête HTTP, valider l'entrée, appeler le service,
 * renvoyer la réponse. AUCUNE règle métier ici : tout est dans AuthService.
 */
// @Tag : regroupe ces endpoints sous "Authentification" dans Swagger UI
@Tag(name = "Authentification", description = "Inscription, connexion et profil courant")
// @RestController : chaque méthode renvoie des données converties en JSON
@RestController
// @RequestMapping : préfixe commun à toutes les URL de la classe
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Créer un compte (rôle USER) et recevoir un JWT")
    // @PostMapping("/register") : répond à POST /api/auth/register
    @PostMapping("/register")
    // @ResponseStatus(CREATED) : renvoie 201 Created au lieu du 200 OK par défaut
    @ResponseStatus(HttpStatus.CREATED)
    // @Valid : vérifie les annotations de RegisterRequest ; @RequestBody : JSON → objet Java
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.inscrire(request);
    }

    @Operation(summary = "Se connecter et recevoir un JWT")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.connecter(request);
    }

    @Operation(summary = "Profil de l'utilisateur connecté")
    @GetMapping("/moi")
    // @AuthenticationPrincipal : injecte le JWT déjà vérifié par Spring Security
    public UtilisateurDto moi(@AuthenticationPrincipal Jwt jwt) {
        return service.moi(jwt.getSubject());   // "sub" du jeton = courriel
    }
}
