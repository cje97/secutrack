import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Role, Utilisateur } from '../core/auth/auth.models';

/**
 * Appels HTTP vers /api/utilisateurs, endpoints réservés aux administrateurs.
 * Appelés par un USER, ils renverraient 403 : le frontend ne les appelle donc que si auth.estAdmin().
 */
@Injectable({ providedIn: 'root' })
export class UtilisateurService {
  private readonly http = inject(HttpClient);

  lister(): Observable<Utilisateur[]> {
    return this.http.get<Utilisateur[]>('/api/utilisateurs');
  }

  changerRole(id: number, role: Role): Observable<Utilisateur> {
    return this.http.patch<Utilisateur>(`/api/utilisateurs/${id}/role`, { role });
  }
}
