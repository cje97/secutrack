package com.secutrack.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestion centralisée des erreurs : au lieu de mettre des try/catch dans chaque contrôleur,
 * les exceptions "remontent" jusqu'ici et sont converties en réponse HTTP propre.
 *
 * Toutes les erreurs sont renvoyées au format standard ProblemDetail (RFC 9457) :
 * <pre>
 * { "type": "about:blank", "title": "Requête invalide", "status": 400,
 *   "detail": "...", "instance": "/api/tickets", "erreurs": { "titre": "Le titre est obligatoire" } }
 * </pre>
 * Angular reçoit donc toujours la même structure, quelle que soit l'erreur.
 */
// @RestControllerAdvice : cette classe intercepte les exceptions levées par TOUS les contrôleurs
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Logger SLF4J : écrit dans les journaux du serveur (console, fichiers), jamais vers le client
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // @ExceptionHandler(X.class) : cette méthode est appelée quand une exception de type X est levée.
    // Spring choisit toujours le handler le plus précis (sous-classe avant classe parente).
    @ExceptionHandler(RessourceIntrouvableException.class)
    public ProblemDetail introuvable(RessourceIntrouvableException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e.getMessage());          // 404
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail regleMetier(BusinessException e) {
        return probleme(HttpStatus.CONFLICT, "Règle métier non respectée", e.getMessage());      // 409
    }

    /** Levée par Hibernate quand @Version détecte que deux personnes ont modifié le même ticket. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail modificationConcurrente(ObjectOptimisticLockingFailureException e) {
        return probleme(HttpStatus.CONFLICT, "Modification concurrente",
                "Le ticket a été modifié par quelqu'un d'autre. Rechargez-le puis recommencez.");
    }

    /**
     * Erreurs de validation (400), avec le détail champ par champ.
     * BindException couvre @Valid sur un @RequestBody (sa sous-classe MethodArgumentNotValidException)
     * ET les filtres reçus en paramètres d'URL (?statut=INCONNU).
     */
    @ExceptionHandler(BindException.class)
    public ProblemDetail requeteInvalide(BindException e) {
        ProblemDetail p = probleme(HttpStatus.BAD_REQUEST, "Requête invalide",
                "Certains champs ne respectent pas les règles de validation");
        Map<String, String> erreurs = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> erreurs.putIfAbsent(err.getField(), err.getDefaultMessage()));
        p.setProperty("erreurs", erreurs);   // propriété personnalisée ajoutée au JSON
        return p;
    }

    /** Paramètre d'URL invalide : id non numérique, tri sur une colonne inconnue (?sort=xyz)... → 400 */
    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class,
            PropertyReferenceException.class})
    public ProblemDetail parametreInvalide(Exception e) {
        return probleme(HttpStatus.BAD_REQUEST, "Paramètre invalide", e.getMessage());
    }

    /** Corps JSON illisible : virgule manquante, ou valeur d'énumération inconnue ("severite": "XYZ") → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail corpsIllisible(HttpMessageNotReadableException e) {
        return probleme(HttpStatus.BAD_REQUEST, "Corps de requête illisible",
                "JSON mal formé ou valeur d'énumération inconnue");
    }

    /** Mauvais courriel ou mot de passe au login → 401. Message volontairement vague (sécurité). */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail authentification(AuthenticationException e) {
        return probleme(HttpStatus.UNAUTHORIZED, "Authentification refusée", "Courriel ou mot de passe invalide");
    }

    /** Levée par @PreAuthorize ou par nos règles dans TicketService → 403. */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accesRefuse(AccessDeniedException e) {
        return probleme(HttpStatus.FORBIDDEN, "Accès refusé", e.getMessage());
    }

    /**
     * Filet de sécurité pour tout le reste (bug imprévu) → 500.
     * On journalise la trace complète côté serveur, mais on ne l'expose JAMAIS au client :
     * une trace Java révèle la structure interne de l'application (information utile à un attaquant).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail inattendue(Exception e) {
        log.error("Erreur non gérée", e);
        return probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", "Une erreur inattendue est survenue");
    }

    /** Construit un ProblemDetail avec un statut, un titre court et un détail lisible. */
    private ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(statut, detail);
        p.setTitle(titre);
        return p;
    }
}
