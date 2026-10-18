import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { Utilisateur } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { messageErreur } from '../../core/erreur-api';
import { NotificationService } from '../../core/notification.service';
import { ConfirmationDialog } from '../../shared/confirmation/confirmation-dialog';
import {
  LIBELLES_SEVERITE,
  LIBELLES_STATUT,
  LIBELLES_TYPE,
  STATUTS,
  STATUTS_ADMIN,
  Statut,
  Ticket,
} from '../../tickets/ticket.models';
import { TicketService } from '../../tickets/ticket.service';
import { UtilisateurService } from '../../tickets/utilisateur.service';

/**
 * Page de détail d'un ticket : informations complètes + actions selon les droits
 * (modifier, changer le statut, assigner, supprimer).
 */
@Component({
  selector: 'app-ticket-detail',
  imports: [
    DatePipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    MatDividerModule,
  ],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.scss',
})
export class TicketDetail implements OnInit {
  // input.required() : paramètre :id de la route (/tickets/7 → "7"), obligatoire
  readonly id = input.required<string>();

  private readonly tickets = inject(TicketService);
  private readonly utilisateurService = inject(UtilisateurService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected readonly libStatut = LIBELLES_STATUT;
  protected readonly libSeverite = LIBELLES_SEVERITE;
  protected readonly libType = LIBELLES_TYPE;

  protected readonly ticket = signal<Ticket | null>(null);
  protected readonly erreur = signal<string | null>(null);
  protected readonly utilisateurs = signal<Utilisateur[]>([]);

  // ---- Droits calculés (miroir des règles du TicketService Java) ----
  // Ils servent uniquement à afficher ou masquer les boutons : le backend revérifie tout.
  private readonly moi = computed(() => this.auth.utilisateur()?.courriel);
  protected readonly estFerme = computed(() => this.ticket()?.statut === 'FERME');
  protected readonly peutModifier = computed(() => {
    const t = this.ticket();
    return !!t && !this.estFerme() && (this.auth.estAdmin() || t.createur.courriel === this.moi());
  });
  protected readonly peutChangerStatut = computed(() => {
    const t = this.ticket();
    return (
      !!t &&
      !this.estFerme() &&
      (this.auth.estAdmin() ||
        t.createur.courriel === this.moi() ||
        t.assigne?.courriel === this.moi())
    );
  });
  /** Un USER ne voit pas RESOLU / FERME dans la liste (sauf le statut actuel). */
  // filter() : un USER ne voit pas RESOLU ni FERME dans la liste déroulante
  protected readonly statutsPermis = computed(() =>
    STATUTS.filter(
      (s) => this.auth.estAdmin() || !STATUTS_ADMIN.includes(s) || s === this.ticket()?.statut,
    ),
  );

  ngOnInit(): void {
    this.charger();
    if (this.auth.estAdmin()) {
      this.utilisateurService.lister().subscribe({ next: (u) => this.utilisateurs.set(u) });
    }
  }

  protected changerStatut(statut: Statut): void {
    const t = this.ticket();
    if (!t || statut === t.statut) {
      return;
    }
    this.tickets.changerStatut(t.id, statut).subscribe({
      next: (maj) => {
        this.ticket.set(maj);
        this.notifications.succes(`Statut : ${LIBELLES_STATUT[maj.statut]}`);
      },
      error: (err) => {
        this.notifications.erreur(messageErreur(err));
        this.charger(); // remet la liste déroulante sur la vraie valeur
      },
    });
  }

  protected assigner(assigneId: number | null): void {
    const t = this.ticket();
    if (!t) {
      return;
    }
    this.tickets.assigner(t.id, assigneId).subscribe({
      next: (maj) => {
        this.ticket.set(maj);
        this.notifications.succes(
          maj.assigne ? `Assigné à ${maj.assigne.nomComplet}` : 'Assignation retirée',
        );
      },
      error: (err) => this.notifications.erreur(messageErreur(err)),
    });
  }

  protected supprimer(): void {
    const t = this.ticket();
    if (!t) {
      return;
    }
    this.dialog
      .open(ConfirmationDialog, {
        data: {
          titre: 'Supprimer le ticket',
          message: `Supprimer définitivement « ${t.titre} » ?`,
          libelleConfirmer: 'Supprimer',
        },
      })
      // afterClosed() : Observable qui émet true (Supprimer) ou false/undefined (Annuler) à la fermeture
      .afterClosed()
      .subscribe((confirme) => {
        if (!confirme) {
          return;
        }
        this.tickets.supprimer(t.id).subscribe({
          next: () => {
            this.notifications.succes(`Ticket #${t.id} supprimé`);
            void this.router.navigate(['/']);
          },
          error: (err) => this.notifications.erreur(messageErreur(err)),
        });
      });
  }

  private charger(): void {
    this.tickets.trouver(Number(this.id())).subscribe({
      next: (t) => {
        this.ticket.set(t);
        this.erreur.set(null);
      },
      error: (err) => this.erreur.set(messageErreur(err)),
    });
  }
}
