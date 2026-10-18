/*
 * Modèles TypeScript des tickets, alignés sur les enums et DTO Java du backend
 * (TypeTicket, Severite, Statut, TicketResponse, TicketRequest...).
 * Les LIBELLES_* traduisent les valeurs techniques ("EN_COURS") en texte affichable ("En cours").
 */

export type TypeTicket = 'INCIDENT' | 'VULNERABILITE' | 'DEMANDE_ACCES' | 'AUTRE';
export type Severite = 'FAIBLE' | 'MOYENNE' | 'ELEVEE' | 'CRITIQUE';
export type Statut = 'OUVERT' | 'EN_COURS' | 'RESOLU' | 'FERME';

export const TYPES: readonly TypeTicket[] = ['INCIDENT', 'VULNERABILITE', 'DEMANDE_ACCES', 'AUTRE'];
export const SEVERITES: readonly Severite[] = ['FAIBLE', 'MOYENNE', 'ELEVEE', 'CRITIQUE'];
export const STATUTS: readonly Statut[] = ['OUVERT', 'EN_COURS', 'RESOLU', 'FERME'];

/** Statuts que seul un ADMIN peut appliquer (même règle que Statut.RESERVES_ADMIN en Java). */
export const STATUTS_ADMIN: readonly Statut[] = ['RESOLU', 'FERME'];

// Record<Cle, Valeur> : objet qui associe un libellé à CHAQUE valeur de l'union.
// Si on ajoute une valeur à TypeTicket sans libellé, TypeScript signale une erreur de compilation.
export const LIBELLES_TYPE: Record<TypeTicket, string> = {
  INCIDENT: 'Incident',
  VULNERABILITE: 'Vulnérabilité',
  DEMANDE_ACCES: "Demande d'accès",
  AUTRE: 'Autre',
};

export const LIBELLES_SEVERITE: Record<Severite, string> = {
  FAIBLE: 'Faible',
  MOYENNE: 'Moyenne',
  ELEVEE: 'Élevée',
  CRITIQUE: 'Critique',
};

export const LIBELLES_STATUT: Record<Statut, string> = {
  OUVERT: 'Ouvert',
  EN_COURS: 'En cours',
  RESOLU: 'Résolu',
  FERME: 'Fermé',
};

export interface UtilisateurResume {
  id: number;
  nomComplet: string;
  courriel: string;
}

export interface Ticket {
  id: number;
  titre: string;
  description: string | null;
  type: TypeTicket;
  severite: Severite;
  statut: Statut;
  createur: UtilisateurResume;
  assigne: UtilisateurResume | null;
  creeLe: string;
  misAJourLe: string;
}

export interface TicketRequest {
  titre: string;
  description: string;
  type: TypeTicket;
  severite: Severite;
}

export interface FiltreTickets {
  statut?: Statut | null;
  severite?: Severite | null;
  type?: TypeTicket | null;
  assigneId?: number | null;
  recherche?: string | null;
}

/** Format de page Spring Data (mode VIA_DTO). */
export interface Page<T> {
  content: T[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}

export interface Statistiques {
  total: number;
  parStatut: Record<Statut, number>;
  actifsParSeverite: Record<Severite, number>;
  assignesAMoi: number;
}
