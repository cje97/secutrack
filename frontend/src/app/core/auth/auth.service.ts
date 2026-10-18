import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest } from './auth.models';

const CLE_SESSION = 'secutrack.session';

/**
 * Gère la session : jeton JWT + utilisateur courant, exposés sous forme de signals.
 *
 * Note de sécurité : le jeton est conservé dans le localStorage pour rester connecté après
 * un rafraîchissement. C'est simple, mais un script injecté (XSS) pourrait le lire. En entreprise,
 * on préfère souvent un cookie HttpOnly ou un fournisseur d'identité (OIDC).
 */
// @Injectable({ providedIn: 'root' }) : service singleton, une seule instance pour toute l'application
// (l'équivalent d'un @Service Spring)
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  // signal() : valeur réactive. Quand elle change (set), tout ce qui la lit est mis à jour
  // automatiquement : templates, computed(), effect(). C'est le cœur de la réactivité d'Angular moderne.
  private readonly session = signal<AuthResponse | null>(this.lireSession());

  // computed() : valeur DÉRIVÉE d'autres signals, recalculée automatiquement.
  // Ex. : quand session change, estAdmin se met à jour, et la barre d'outils affiche ou masque "Utilisateurs".

  readonly utilisateur = computed(() => this.session()?.utilisateur ?? null);
  readonly estConnecte = computed(() => this.session() !== null);
  readonly estAdmin = computed(() => this.utilisateur()?.role === 'ADMIN');

  connecter(request: LoginRequest): Observable<AuthResponse> {
    return (
      this.http
        .post<AuthResponse>('/api/auth/login', request)
        // pipe(tap(...)) : exécute un effet de bord (enregistrer la session) sans modifier la réponse
        .pipe(tap((reponse) => this.ouvrirSession(reponse)))
    );
  }

  inscrire(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>('/api/auth/register', request)
      .pipe(tap((reponse) => this.ouvrirSession(reponse)));
  }

  /** Jeton valide, ou null s'il est absent ou expiré (la session est alors fermée). */
  jeton(): string | null {
    const session = this.session();
    if (!session) {
      return null;
    }
    if (new Date(session.expiresAt).getTime() <= Date.now()) {
      this.fermerSession();
      return null;
    }
    return session.token;
  }

  deconnecter(): void {
    this.fermerSession();
    void this.router.navigate(['/connexion']);
  }

  private ouvrirSession(reponse: AuthResponse): void {
    localStorage.setItem(CLE_SESSION, JSON.stringify(reponse));
    this.session.set(reponse);
  }

  private fermerSession(): void {
    localStorage.removeItem(CLE_SESSION);
    this.session.set(null);
  }

  private lireSession(): AuthResponse | null {
    try {
      const brut = localStorage.getItem(CLE_SESSION);
      if (!brut) {
        return null;
      }
      const session = JSON.parse(brut) as AuthResponse;
      return new Date(session.expiresAt).getTime() > Date.now() ? session : null;
    } catch {
      return null;
    }
  }
}
