/*
 * Données de test partagées entre les fichiers .spec.ts (sessions USER/ADMIN, ticket exemple).
 * connecterEnTantQue() écrit une session dans le localStorage, comme le ferait un vrai login.
 */
import { AuthResponse } from '../core/auth/auth.models';
import { Ticket } from '../tickets/ticket.models';

/** Données partagées par les fichiers .spec.ts */
export function session(role: 'USER' | 'ADMIN', minutesAvantExpiration = 60): AuthResponse {
  return {
    token: `jeton-${role.toLowerCase()}`,
    expiresAt: new Date(Date.now() + minutesAvantExpiration * 60_000).toISOString(),
    utilisateur: {
      id: role === 'ADMIN' ? 1 : 2,
      nomComplet: role === 'ADMIN' ? 'Alice Admin' : 'Bruno Analyste',
      courriel: role === 'ADMIN' ? 'admin@secutrack.local' : 'analyste@secutrack.local',
      role,
      creeLe: '2026-10-01T12:00:00Z',
    },
  };
}

export function connecterEnTantQue(role: 'USER' | 'ADMIN'): void {
  localStorage.setItem('secutrack.session', JSON.stringify(session(role)));
}

export const TICKET: Ticket = {
  id: 7,
  titre: 'Campagne d’hameçonnage',
  description: 'Courriels frauduleux',
  type: 'INCIDENT',
  severite: 'CRITIQUE',
  statut: 'OUVERT',
  createur: { id: 2, nomComplet: 'Bruno Analyste', courriel: 'analyste@secutrack.local' },
  assigne: null,
  creeLe: '2026-10-19T13:00:00Z',
  misAJourLe: '2026-10-19T13:00:00Z',
};
