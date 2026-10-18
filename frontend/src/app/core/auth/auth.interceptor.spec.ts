/*
 * Tests unitaires (auth.interceptor) exécutés par Vitest : npx ng test --watch=false
 *
 * TestBed : crée un mini-environnement Angular pour le test (comme @SpringBootTest, en plus léger).
 * provideHttpClientTesting + HttpTestingController : aucune vraie requête ne part ;
 *   expectOne(url) vérifie qu'une requête a été envoyée, flush(données) simule la réponse du serveur.
 * describe() regroupe les tests ; it() est un test ; expect(...).toBe(...) est une vérification.
 */
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { connecterEnTantQue } from '../../testing/donnees-test';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controle: HttpTestingController;

  function configurer(): void {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'connexion', children: [] }]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controle = TestBed.inject(HttpTestingController);
  }

  beforeEach(() => localStorage.clear());

  it('ajoute le header Authorization quand l’utilisateur est connecté', () => {
    connecterEnTantQue('USER');
    configurer();

    http.get('/api/tickets').subscribe();

    const req = controle.expectOne('/api/tickets');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jeton-user');
    req.flush({});
  });

  it('n’ajoute rien sans session', () => {
    configurer();

    http.get('/api/tickets').subscribe({ error: () => undefined });

    const req = controle.expectOne('/api/tickets');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({}, { status: 401, statusText: 'Unauthorized' });
  });

  it('ferme la session si l’API répond 401', () => {
    connecterEnTantQue('USER');
    configurer();
    const auth = TestBed.inject(AuthService);

    http.get('/api/tickets').subscribe({ error: () => undefined });
    controle.expectOne('/api/tickets').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.estConnecte()).toBe(false);
  });
});
