/*
 * Types TypeScript qui reproduisent les DTO Java du backend (UtilisateurDto, AuthResponse...).
 * Ils n'existent qu'à la compilation : ils servent à détecter les erreurs de frappe et à avoir
 * l'autocomplétion dans l'éditeur. Si un DTO Java change, il faut mettre à jour ce fichier.
 */

// Type "union" : une variable de type Role ne peut valoir que 'USER' ou 'ADMIN'
export type Role = 'USER' | 'ADMIN';

export interface Utilisateur {
  id: number;
  nomComplet: string;
  courriel: string;
  role: Role;
  creeLe: string;
}

export interface AuthResponse {
  token: string;
  expiresAt: string;
  utilisateur: Utilisateur;
}

export interface LoginRequest {
  courriel: string;
  motDePasse: string;
}

export interface RegisterRequest {
  nomComplet: string;
  courriel: string;
  motDePasse: string;
}
