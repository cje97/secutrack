import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

export interface DonneesConfirmation {
  titre: string;
  message: string;
  libelleConfirmer: string;
}

/**
 * Boîte de dialogue générique de confirmation (Angular Material).
 *
 * Utilisation :
 *   this.dialog.open(ConfirmationDialog, { data: { titre, message, libelleConfirmer } })
 *     .afterClosed()
 *     .subscribe((confirme) => { if (confirme) { ... } });
 *
 * [mat-dialog-close]="true" ferme la boîte et renvoie true à afterClosed().
 * Template "inline" : le HTML est directement dans le décorateur (pratique pour un petit composant).
 */
@Component({
  selector: 'app-confirmation-dialog',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ donnees.titre }}</h2>
    <mat-dialog-content>{{ donnees.message }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="false">Annuler</button>
      <button mat-flat-button class="bouton-danger" [mat-dialog-close]="true">
        {{ donnees.libelleConfirmer }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmationDialog {
  // MAT_DIALOG_DATA : jeton d'injection qui contient l'objet "data" passé à dialog.open()
  protected readonly donnees = inject<DonneesConfirmation>(MAT_DIALOG_DATA);
}
