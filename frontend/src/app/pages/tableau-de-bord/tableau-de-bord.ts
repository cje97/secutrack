import { DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { messageErreur } from '../../core/erreur-api';
import { NotificationService } from '../../core/notification.service';
import { ConfirmationDialog } from '../../shared/confirmation/confirmation-dialog';
import {
  FiltreTickets,
  LIBELLES_SEVERITE,
  LIBELLES_STATUT,
  LIBELLES_TYPE,
  SEVERITES,
  STATUTS,
  Severite,
  Statistiques,
  Statut,
  TYPES,
  Ticket,
  TypeTicket,
} from '../../tickets/ticket.models';
import { TicketService } from '../../tickets/ticket.service';

/**
 * Tableau de bord : statistiques + liste paginée, triable et filtrable des tickets.
 *
 * Principe réactif : chaque critère (filtres, page, tri) est un signal. Un effect() relance
 * la recherche dès que l'un d'eux change ; il n'y a aucun "bouton Rechercher" à gérer.
 */
@Component({
  selector: 'app-tableau-de-bord',
  imports: [
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
  ],
  templateUrl: './tableau-de-bord.html',
  styleUrl: './tableau-de-bord.scss',
})
export class TableauDeBord {
  private readonly tickets = inject(TicketService);
  private readonly dialog = inject(MatDialog);
  private readonly notifications = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly auth = inject(AuthService);

  protected readonly statuts = STATUTS;
  protected readonly severites = SEVERITES;
  protected readonly types = TYPES;
  protected readonly libStatut = LIBELLES_STATUT;
  protected readonly libSeverite = LIBELLES_SEVERITE;
  protected readonly libType = LIBELLES_TYPE;

  /** Colonnes du tableau : la colonne "actions" n'existe que pour un ADMIN. */
  protected readonly colonnes = computed(() => {
    const base = ['id', 'titre', 'type', 'severite', 'statut', 'assigne', 'creeLe'];
    return this.auth.estAdmin() ? [...base, 'actions'] : base;
  });

  // ---- Critères de recherche (signals) ----
  // Champ de recherche : on attend 300 ms après la dernière frappe (debounceTime) avant de chercher,
  // pour ne pas envoyer une requête à chaque lettre. toSignal() convertit l'Observable en signal.
  protected readonly rechercheCtrl = new FormControl('', { nonNullable: true });
  private readonly recherche = toSignal(
    this.rechercheCtrl.valueChanges.pipe(
      debounceTime(300),
      map((v) => v.trim()),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );
  protected readonly statut = signal<Statut | null>(null);
  protected readonly severite = signal<Severite | null>(null);
  protected readonly type = signal<TypeTicket | null>(null);
  protected readonly assignesAMoi = signal(false);
  protected readonly pageIndex = signal(0);
  protected readonly pageTaille = signal(10);
  protected readonly tri = signal('creeLe,desc');
  private readonly rafraichissement = signal(0);

  private readonly filtre = computed<FiltreTickets>(() => ({
    recherche: this.recherche(),
    statut: this.statut(),
    severite: this.severite(),
    type: this.type(),
    assigneId: this.assignesAMoi() ? (this.auth.utilisateur()?.id ?? null) : null,
  }));

  // ---- État affiché ----
  protected readonly lignes = signal<Ticket[]>([]);
  protected readonly total = signal(0);
  protected readonly chargement = signal(false);
  protected readonly erreur = signal<string | null>(null);
  protected readonly stats = signal<Statistiques | null>(null);

  constructor() {
    // Revenir à la première page dès qu'un filtre change
    effect(() => {
      this.filtre();
      this.pageIndex.set(0);
    });

    // effect() : s'exécute une première fois, puis à chaque changement d'un signal lu à l'intérieur.
    // Recharger dès qu'un critère change (filtre, page, tri ou rafraîchissement manuel)
    effect((nettoyage) => {
      const filtre = this.filtre();
      const page = this.pageIndex();
      const taille = this.pageTaille();
      const tri = this.tri();
      this.rafraichissement();

      this.chargement.set(true);
      const abonnement = this.tickets.rechercher(filtre, page, taille, tri).subscribe({
        next: (resultat) => {
          this.lignes.set(resultat.content);
          this.total.set(resultat.page.totalElements);
          this.erreur.set(null);
          this.chargement.set(false);
        },
        error: (err) => {
          this.erreur.set(messageErreur(err));
          this.chargement.set(false);
        },
      });
      nettoyage(() => abonnement.unsubscribe()); // annule la requête précédente si un critère change
    });

    this.chargerStatistiques();
  }

  // Les cellules de mat-table sont typées "any" : ces méthodes rétablissent le typage
  protected libelleType(t: Ticket): string {
    return LIBELLES_TYPE[t.type];
  }

  protected libelleSeverite(t: Ticket): string {
    return LIBELLES_SEVERITE[t.severite];
  }

  protected libelleStatut(t: Ticket): string {
    return LIBELLES_STATUT[t.statut];
  }

  protected changerPage(evenement: PageEvent): void {
    this.pageIndex.set(evenement.pageIndex);
    this.pageTaille.set(evenement.pageSize);
  }

  protected trier(evenement: Sort): void {
    this.tri.set(
      evenement.direction ? `${evenement.active},${evenement.direction}` : 'creeLe,desc',
    );
  }

  protected reinitialiser(): void {
    this.rechercheCtrl.setValue('');
    this.statut.set(null);
    this.severite.set(null);
    this.type.set(null);
    this.assignesAMoi.set(false);
  }

  protected ouvrir(ticket: Ticket): void {
    void this.router.navigate(['/tickets', ticket.id]);
  }

  protected supprimer(ticket: Ticket, evenement: Event): void {
    evenement.stopPropagation(); // ne pas ouvrir le détail en cliquant sur la corbeille
    this.dialog
      .open(ConfirmationDialog, {
        data: {
          titre: 'Supprimer le ticket',
          message: `Supprimer définitivement « ${ticket.titre} » ?`,
          libelleConfirmer: 'Supprimer',
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirme) => {
        if (!confirme) {
          return;
        }
        this.tickets.supprimer(ticket.id).subscribe({
          next: () => {
            this.notifications.succes(`Ticket #${ticket.id} supprimé`);
            this.rafraichissement.update((n) => n + 1);
            this.chargerStatistiques();
          },
          error: (err) => this.notifications.erreur(messageErreur(err)),
        });
      });
  }

  private chargerStatistiques(): void {
    this.tickets.statistiques().subscribe({
      next: (s) => this.stats.set(s),
      error: () => this.stats.set(null),
    });
  }
}
