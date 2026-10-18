package com.secutrack.common;

/**
 * Exception métier : une règle de gestion n'est pas respectée
 * (ex. : modifier un ticket fermé, s'inscrire avec un courriel déjà utilisé).
 * Le GlobalExceptionHandler la transforme en réponse HTTP 409 Conflict.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
