import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Affiche des messages temporaires en bas de l'écran ("snackbar" Angular Material).
 * Centraliser ici évite de répéter la configuration (durée, style) dans chaque page.
 */
// @Injectable({ providedIn: 'root' }) : service singleton disponible partout
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  succes(message: string): void {
    this.snackBar.open(message, 'OK', { duration: 3000 });
  }

  erreur(message: string): void {
    this.snackBar.open(message, 'Fermer', { duration: 6000, panelClass: 'snack-erreur' });
  }
}
