import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Ajoute "Authorization: Bearer <jeton>" à chaque appel vers /api,
 * et ferme la session si l'API répond 401 (jeton expiré ou invalide).
 */
// HttpInterceptorFn : fonction appelée pour CHAQUE requête HttpClient.
// req = la requête sortante ; next(req) = l'envoie et renvoie un Observable de la réponse.
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const jeton = auth.jeton();

  // Les requêtes HttpClient sont immuables : clone() crée une copie avec le header en plus
  const requete =
    jeton && req.url.startsWith('/api/')
      ? req.clone({ setHeaders: { Authorization: `Bearer ${jeton}` } })
      : req;

  return next(requete).pipe(
    // catchError : intercepte les erreurs HTTP de la réponse
    catchError((err: unknown) => {
      const estAppelAuth =
        req.url.startsWith('/api/auth/login') || req.url.startsWith('/api/auth/register');
      if (err instanceof HttpErrorResponse && err.status === 401 && !estAppelAuth) {
        auth.deconnecter();
      }
      // On relance l'erreur pour que le composant appelant puisse aussi l'afficher
      return throwError(() => err);
    }),
  );
};
