package com.secutrack.common;

/**
 * Exception métier : la ressource demandée n'existe pas (ticket ou utilisateur).
 * Le GlobalExceptionHandler la transforme en réponse HTTP 404 Not Found.
 *
 * Elle étend RuntimeException (exception "non vérifiée") pour deux raisons :
 *   - pas besoin de "throws" partout dans les signatures ;
 *   - @Transactional annule (rollback) la transaction uniquement pour les RuntimeException.
 */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
