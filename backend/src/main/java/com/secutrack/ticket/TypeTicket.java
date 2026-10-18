package com.secutrack.ticket;

/** Nature du ticket. Stockée en texte en base grâce à @Enumerated(EnumType.STRING) dans Ticket. */
public enum TypeTicket {
    /** Événement de sécurité en cours ou passé (hameçonnage, maliciel, perte de matériel...). */
    INCIDENT,
    /** Faiblesse détectée dans un système ou une bibliothèque. */
    VULNERABILITE,
    /** Demande d'ouverture ou de modification d'accès. */
    DEMANDE_ACCES,
    AUTRE
}
