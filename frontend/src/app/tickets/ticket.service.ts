import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FiltreTickets, Page, Statistiques, Statut, Ticket, TicketRequest } from './ticket.models';

/**
 * Appels HTTP vers /api/tickets. Chaque méthode renvoie un Observable :
 * la requête n'est envoyée que lorsqu'un composant appelle .subscribe().
 * Le JWT est ajouté automatiquement par authInterceptor : rien à faire ici.
 */
@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/tickets';

  /** GET /api/tickets?statut=..&page=0&size=10&sort=creeLe,desc — les filtres vides sont ignorés. */
  rechercher(
    filtre: FiltreTickets,
    page: number,
    taille: number,
    tri: string,
  ): Observable<Page<Ticket>> {
    let params = new HttpParams().set('page', page).set('size', taille).set('sort', tri);
    for (const [cle, valeur] of Object.entries(filtre)) {
      if (valeur !== null && valeur !== undefined && String(valeur).trim() !== '') {
        params = params.set(cle, String(valeur).trim());
      }
    }
    return this.http.get<Page<Ticket>>(this.url, { params });
  }

  statistiques(): Observable<Statistiques> {
    return this.http.get<Statistiques>(`${this.url}/statistiques`);
  }

  trouver(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.url}/${id}`);
  }

  creer(request: TicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(this.url, request);
  }

  modifier(id: number, request: TicketRequest): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.url}/${id}`, request);
  }

  changerStatut(id: number, statut: Statut): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.url}/${id}/statut`, { statut });
  }

  assigner(id: number, assigneId: number | null): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.url}/${id}/assignation`, { assigneId });
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
