import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { Role, Utilisateur } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { messageErreur } from '../../core/erreur-api';
import { NotificationService } from '../../core/notification.service';
import { UtilisateurService } from '../../tickets/utilisateur.service';

/** Page d'administration (route protégée par adminGuard). */
@Component({
  selector: 'app-utilisateurs',
  imports: [DatePipe, MatTableModule, MatSelectModule, MatFormFieldModule],
  templateUrl: './utilisateurs.html',
})
export class Utilisateurs {
  private readonly service = inject(UtilisateurService);
  private readonly notifications = inject(NotificationService);
  protected readonly auth = inject(AuthService);

  // Colonnes affichées par mat-table, dans l'ordre (chaque nom correspond à un matColumnDef du HTML)
  protected readonly colonnes = ['nomComplet', 'courriel', 'creeLe', 'role'];
  protected readonly utilisateurs = signal<Utilisateur[]>([]);

  constructor() {
    this.charger();
  }

  protected changerRole(u: Utilisateur, role: Role): void {
    this.service.changerRole(u.id, role).subscribe({
      next: (maj) => {
        // update() : calcule la nouvelle valeur du signal à partir de l'ancienne.
        // On crée un NOUVEAU tableau (map) au lieu de modifier l'ancien : c'est ce qui déclenche l'affichage.
        this.utilisateurs.update((liste) => liste.map((x) => (x.id === maj.id ? maj : x)));
        this.notifications.succes(
          `${maj.nomComplet} est maintenant ${maj.role === 'ADMIN' ? 'administrateur' : 'utilisateur'}`,
        );
      },
      error: (err) => {
        this.notifications.erreur(messageErreur(err));
        this.charger(); // en cas d'erreur, on recharge pour réafficher le vrai rôle
      },
    });
  }

  private charger(): void {
    this.service.lister().subscribe({
      next: (liste) => this.utilisateurs.set(liste),
      error: (err) => this.notifications.erreur(messageErreur(err)),
    });
  }
}
