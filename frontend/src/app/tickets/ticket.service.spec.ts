/*
 * Tests unitaires (ticket.service) exécutés par Vitest : npx ng test --watch=false
 *
 * TestBed : crée un mini-environnement Angular pour le test (comme @SpringBootTest, en plus léger).
 * provideHttpClientTesting + HttpTestingController : aucune vraie requête ne part ;
 *   expectOne(url) vérifie qu'une requête a été envoyée, flush(données) simule la réponse du serveur.
 * describe() regroupe les tests ; it() est un test ; expect(...).toBe(...) est une vérification.
 */
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TicketService } from './ticket.service';

describe('TicketService', () => {
  let service: TicketService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TicketService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('rechercher() envoie pagination, tri et seulement les filtres renseignés', () => {
    service
      .rechercher({ statut: 'OUVERT', severite: null, recherche: '  vpn ' }, 2, 25, 'titre,asc')
      .subscribe();

    const req = http.expectOne((r) => r.url === '/api/tickets');
    expect(req.request.params.get('statut')).toBe('OUVERT');
    expect(req.request.params.get('recherche')).toBe('vpn');
    expect(req.request.params.has('severite')).toBe(false);
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('25');
    expect(req.request.params.get('sort')).toBe('titre,asc');
    req.flush({ content: [], page: { size: 25, number: 2, totalElements: 0, totalPages: 0 } });
  });

  it('assigner(null) retire l’assignation', () => {
    service.assigner(7, null).subscribe();

    const req = http.expectOne('/api/tickets/7/assignation');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ assigneId: null });
    req.flush({});
  });
});
