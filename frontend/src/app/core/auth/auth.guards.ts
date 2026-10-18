import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/*
 * Guards : fonctions exécutées par le routeur AVANT d'afficher une page.
 * Elles renvoient true (accès autorisé) ou un UrlTree (redirection vers une autre page).
 *
 * Attention : un guard améliore l'expérience utilisateur, il ne SÉCURISE rien.
 * Le code Angular s'exécute dans le navigateur et peut être contourné ; la vraie sécurité
 * est dans le backend (Spring Security, @PreAuthorize, TicketService).
 */

/** Routes réservées aux utilisateurs connectés ; mémorise l'URL demandée pour y revenir. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  return auth.jeton()
    ? true
    : inject(Router).createUrlTree(['/connexion'], { queryParams: { retour: state.url } });
};

/** Routes réservées aux administrateurs. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estAdmin() ? true : inject(Router).createUrlTree(['/']);
};

/** Pages de connexion et d'inscription : inutiles si on est déjà connecté. */
export const invitesGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.jeton() ? inject(Router).createUrlTree(['/']) : true;
};
