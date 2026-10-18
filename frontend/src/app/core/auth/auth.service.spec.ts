/*
 * Tests unitaires (auth.service) exécutés par Vitest : npx ng test --watch=false
 *
 * TestBed : crée un mini-environnement Angular pour le test (comme @SpringBootTest, en plus léger).
 * provideHttpClientTesting + HttpTestingController : aucune vraie requête ne part ;
 *   expectOne(url) vérifie qu'une requête a été envoyée, flush(données) simule la réponse du serveur.
 * describe() regroupe les tests ; it() est un test ; expect(...).toBe(...) est une vérification.
 */
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { connecterEnTantQue, session } from '../../testing/donnees-test';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'connexion', children: [] }]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
  });

  it('connecter() enregistre la session et expose le rôle', () => {
    const auth = TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);

    auth.connecter({ courriel: 'admin@secutrack.local', motDePasse: 'x' }).subscribe();
    http.expectOne('/api/auth/login').flush(session('ADMIN'));

    expect(auth.estConnecte()).toBe(true);
    expect(auth.estAdmin()).toBe(true);
    expect(auth.jeton()).toBe('jeton-admin');
    expect(localStorage.getItem('secutrack.session')).not.toBeNull();
  });

  it('restaure la session au démarrage (rafraîchissement de page)', () => {
    connecterEnTantQue('USER');
    const auth = TestBed.inject(AuthService);

    expect(auth.utilisateur()?.nomComplet).toBe('Bruno Analyste');
    expect(auth.estAdmin()).toBe(false);
  });

  it('ignore une session expirée', () => {
    localStorage.setItem('secutrack.session', JSON.stringify(session('ADMIN', -5)));
    const auth = TestBed.inject(AuthService);

    expect(auth.estConnecte()).toBe(false);
    expect(auth.jeton()).toBeNull();
  });

  it('deconnecter() efface la session', () => {
    connecterEnTantQue('ADMIN');
    const auth = TestBed.inject(AuthService);

    auth.deconnecter();

    expect(auth.estConnecte()).toBe(false);
    expect(localStorage.getItem('secutrack.session')).toBeNull();
  });
});
