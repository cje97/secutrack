package com.secutrack.utilisateur;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints d'administration des utilisateurs, réservés aux ADMIN.
 */
// @Tag : regroupe ces endpoints sous "Utilisateurs" dans Swagger UI
@Tag(name = "Utilisateurs", description = "Administration des comptes (ADMIN)")
// @RestController = @Controller + @ResponseBody : chaque méthode renvoie des données
// converties automatiquement en JSON par Jackson (et non une page HTML)
@RestController
// @RequestMapping : préfixe d'URL commun à toutes les méthodes de la classe
@RequestMapping("/api/utilisateurs")
// @PreAuthorize sur la classe : s'applique à toutes les méthodes. Double protection avec
// la règle d'URL de SecuriteConfig : si l'une des deux est oubliée un jour, l'autre protège encore.
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService service;

    public UtilisateurController(UtilisateurService service) {
        this.service = service;
    }

    // @Operation : description affichée dans Swagger UI
    @Operation(summary = "Lister les utilisateurs (pour l'assignation et l'administration)")
    // @GetMapping : répond à GET /api/utilisateurs
    @GetMapping
    public List<UtilisateurDto> lister() {
        return service.lister();
    }

    @Operation(summary = "Changer le rôle d'un utilisateur")
    // @PatchMapping("/{id}/role") : répond à PATCH /api/utilisateurs/42/role (modification partielle)
    @PatchMapping("/{id}/role")
    public UtilisateurDto changerRole(
            // @PathVariable : lit {id} dans l'URL → 42
            @PathVariable Long id,
            // @Valid : déclenche la validation de RoleRequest (@NotNull) ; échec → 400
            // @RequestBody : convertit le JSON du corps de la requête en objet Java
            @Valid @RequestBody RoleRequest request,
            // @AuthenticationPrincipal : injecte le JWT de l'utilisateur connecté (déjà vérifié par Spring Security)
            @AuthenticationPrincipal Jwt jwt) {
        // jwt.getSubject() = la claim "sub" du jeton = le courriel de l'utilisateur connecté
        return service.changerRole(id, request.role(), jwt.getSubject());
    }
}
