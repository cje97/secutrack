/*
 * Tests unitaires (ticket-formulaire) exécutés par Vitest : npx ng test --watch=false
 *
 * TestBed : crée un mini-environnement Angular pour le test (comme @SpringBootTest, en plus léger).
 * provideHttpClientTesting + HttpTestingController : aucune vraie requête ne part ;
 *   expectOne(url) vérifie qu'une requête a été envoyée, flush(données) simule la réponse du serveur.
 * describe() regroupe les tests ; it() est un test ; expect(...).toBe(...) est une vérification.
 */
import { FormControl } from '@angular/forms';
import { nonVide } from './ticket-formulaire';

describe('nonVide (équivalent de @NotBlank)', () => {
  it('refuse une chaîne vide ou composée d’espaces', () => {
    expect(nonVide(new FormControl(''))).toEqual({ nonVide: true });
    expect(nonVide(new FormControl('   '))).toEqual({ nonVide: true });
  });

  it('accepte un vrai titre', () => {
    expect(nonVide(new FormControl('Phishing'))).toBeNull();
  });
});
