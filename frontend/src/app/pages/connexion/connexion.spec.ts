/*
 * Tests unitaires (connexion) exécutés par Vitest : npx ng test --watch=false
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
import { Connexion } from './connexion';

describe('Connexion', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [Connexion],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it("n'appelle pas l'API si le formulaire est invalide", async () => {
    const fixture = TestBed.createComponent(Connexion);
    await fixture.whenStable();

    (fixture.nativeElement as HTMLElement)
      .querySelector('form')!
      .dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    TestBed.inject(HttpTestingController).expectNone('/api/auth/login');
    expect(fixture.nativeElement.textContent).toContain('Le courriel est obligatoire');
  });

  it('affiche un message clair si les identifiants sont refusés (401)', async () => {
    const fixture = TestBed.createComponent(Connexion);
    await fixture.whenStable();
    const page = fixture.nativeElement as HTMLElement;

    page.querySelectorAll<HTMLButtonElement>('.demo button')[0].click(); // remplit le compte admin de démo
    page.querySelector('form')!.dispatchEvent(new Event('submit'));
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/login')
      .flush({ title: 'Authentification refusée' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(page.querySelector('[role="alert"]')?.textContent).toContain(
      'Courriel ou mot de passe invalide',
    );
  });
});
