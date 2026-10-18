import { Routes } from '@angular/router';
import { adminGuard, authGuard, invitesGuard } from './core/auth/auth.guards';

/**
 * Table de routage : quelle URL affiche quel composant.
 *
 * - loadComponent : la page est chargée à la demande (lazy loading), le bundle initial reste léger.
 * - canActivate   : "guards" exécutés avant d'afficher la page (voir core/auth/auth.guards.ts).
 * - children      : toutes les routes enfants héritent du guard du parent (authGuard).
 * - title         : texte de l'onglet du navigateur.
 */
export const routes: Routes = [
  {
    path: 'connexion',
    canActivate: [invitesGuard],
    title: 'Connexion — SecuTrack',
    loadComponent: () => import('./pages/connexion/connexion').then((m) => m.Connexion),
  },
  {
    path: 'inscription',
    canActivate: [invitesGuard],
    title: 'Inscription — SecuTrack',
    loadComponent: () => import('./pages/inscription/inscription').then((m) => m.Inscription),
  },
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        title: 'Tableau de bord — SecuTrack',
        loadComponent: () =>
          import('./pages/tableau-de-bord/tableau-de-bord').then((m) => m.TableauDeBord),
      },
      {
        path: 'tickets/nouveau',
        title: 'Nouveau ticket — SecuTrack',
        loadComponent: () =>
          import('./pages/ticket-formulaire/ticket-formulaire').then((m) => m.TicketFormulaire),
      },
      {
        path: 'tickets/:id',
        title: 'Ticket — SecuTrack',
        loadComponent: () =>
          import('./pages/ticket-detail/ticket-detail').then((m) => m.TicketDetail),
      },
      {
        path: 'tickets/:id/modifier',
        title: 'Modifier le ticket — SecuTrack',
        loadComponent: () =>
          import('./pages/ticket-formulaire/ticket-formulaire').then((m) => m.TicketFormulaire),
      },
      {
        path: 'utilisateurs',
        canActivate: [adminGuard],
        title: 'Utilisateurs — SecuTrack',
        loadComponent: () =>
          import('./pages/utilisateurs/utilisateurs').then((m) => m.Utilisateurs),
      },
    ],
  },
  // '**' : toute URL inconnue renvoie au tableau de bord
  { path: '**', redirectTo: '' },
];
