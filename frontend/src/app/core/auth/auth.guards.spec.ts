/*
 * Tests unitaires (auth.guards) exécutés par Vitest : npx ng test --watch=false
 *
 * TestBed : crée un mini-environnement Angular pour le test (comme @SpringBootTest, en plus léger).
 * provideHttpClientTesting + HttpTestingController : aucune vraie requête ne part ;
 *   expectOne(url) vérifie qu'une requête a été envoyée, flush(données) simule la réponse du serveur.
 * describe() regroupe les tests ; it() est un test ; expect(...).toBe(...) est une vérification.
 */
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { connecterEnTantQue } from '../../testing/donnees-test';
import { adminGuard, authGuard } from './auth.guards';

describe('Guards', () => {
  const route = {} as ActivatedRouteSnapshot;
  const etat = { url: '/tickets/7' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
  });

  function configurer(): void {
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
  }

  it('authGuard redirige vers /connexion en mémorisant la page demandée', () => {
    configurer();
    const resultat = TestBed.runInInjectionContext(() => authGuard(route, etat)) as UrlTree;

    expect(TestBed.inject(Router).serializeUrl(resultat)).toBe('/connexion?retour=%2Ftickets%2F7');
  });

  it('authGuard laisse passer un utilisateur connecté', () => {
    connecterEnTantQue('USER');
    configurer();

    expect(TestBed.runInInjectionContext(() => authGuard(route, etat))).toBe(true);
  });

  it('adminGuard refuse un USER et accepte un ADMIN', () => {
    connecterEnTantQue('USER');
    configurer();
    expect(TestBed.runInInjectionContext(() => adminGuard(route, etat))).toBeInstanceOf(UrlTree);

    TestBed.resetTestingModule();
    connecterEnTantQue('ADMIN');
    configurer();
    expect(TestBed.runInInjectionContext(() => adminGuard(route, etat))).toBe(true);
  });
});
