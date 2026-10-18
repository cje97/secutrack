import { HttpErrorResponse } from '@angular/common/http';

/*
 * Utilitaires de gestion des erreurs HTTP.
 * Le backend renvoie toujours ses erreurs au format ProblemDetail (voir GlobalExceptionHandler.java) :
 * ces fonctions les transforment en messages lisibles pour l'utilisateur.
 */

/** Format ProblemDetail (RFC 9457) renvoyé par le GlobalExceptionHandler Spring. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  erreurs?: Record<string, string>;
}

/** Transforme n'importe quelle erreur HTTP en message lisible. */
export function messageErreur(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    // status 0 : la requête n'a jamais atteint le serveur (backend arrêté, réseau coupé)
    if (err.status === 0) {
      return "Impossible de joindre l'API. Le backend est-il démarré ?";
    }
    const probleme = err.error as ProblemDetail | null;
    return probleme?.detail ?? probleme?.title ?? `Erreur ${err.status}`;
  }
  return 'Une erreur inattendue est survenue.';
}

/** Erreurs de validation par champ (clé "erreurs" du ProblemDetail). */
export function erreursParChamp(err: unknown): Record<string, string> {
  if (err instanceof HttpErrorResponse) {
    return (err.error as ProblemDetail | null)?.erreurs ?? {};
  }
  return {};
}
