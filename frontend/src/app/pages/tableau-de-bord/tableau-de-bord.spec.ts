/*
 * Tests unitaires (tableau-de-bord) exécutés par Vitest : npx ng test --watch=false
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
import { TICKET, connecterEnTantQue } from '../../testing/donnees-test';
import { TableauDeBord } from './tableau-de-bord';

describe('TableauDeBord', () => {
  beforeEach(() => localStorage.clear());

  async function afficher() {
    TestBed.configureTestingModule({
      imports: [TableauDeBord],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(TableauDeBord);
    await fixture.whenStable();

    http.expectOne('/api/tickets/statistiques').flush({
      total: 1,
      parStatut: { OUVERT: 1, EN_COURS: 0, RESOLU: 0, FERME: 0 },
      actifsParSeverite: { FAIBLE: 0, MOYENNE: 0, ELEVEE: 0, CRITIQUE: 1 },
      assignesAMoi: 0,
    });
    for (const req of http.match((r) => r.url === '/api/tickets')) {
      req.flush({
        content: [TICKET],
        page: { size: 10, number: 0, totalElements: 1, totalPages: 1 },
      });
    }
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('affiche les tickets renvoyés par l’API', async () => {
    connecterEnTantQue('USER');
    const page = await afficher();

    expect(page.querySelectorAll('tr.ligne').length).toBe(1);
    expect(page.textContent).toContain('Campagne d’hameçonnage');
    expect(page.textContent).toContain('Critique');
  });

  it('un USER ne voit pas le bouton Supprimer', async () => {
    connecterEnTantQue('USER');
    const page = await afficher();

    expect(page.querySelector('.bouton-supprimer')).toBeNull();
  });

  it('un ADMIN voit le bouton Supprimer', async () => {
    connecterEnTantQue('ADMIN');
    const page = await afficher();

    expect(page.querySelector('.bouton-supprimer')).not.toBeNull();
  });
});
