import { Component, OnInit, inject, input, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { erreursParChamp, messageErreur } from '../../core/erreur-api';
import { NotificationService } from '../../core/notification.service';
import {
  LIBELLES_SEVERITE,
  LIBELLES_TYPE,
  SEVERITES,
  Severite,
  TYPES,
  TicketRequest,
  TypeTicket,
} from '../../tickets/ticket.models';
import { TicketService } from '../../tickets/ticket.service';

/**
 * Validateur personnalisé, équivalent de @NotBlank côté Java : refuse une chaîne vide ou composée d'espaces.
 * Un validateur renvoie null si la valeur est valide, ou un objet décrivant l'erreur.
 */
export function nonVide(control: AbstractControl): ValidationErrors | null {
  return String(control.value ?? '').trim().length > 0 ? null : { nonVide: true };
}

/** Formulaire de création (/tickets/nouveau) et de modification (/tickets/:id/modifier). */
@Component({
  selector: 'app-ticket-formulaire',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  templateUrl: './ticket-formulaire.html',
  styleUrl: './ticket-formulaire.scss',
})
export class TicketFormulaire implements OnInit {
  /** Paramètre :id de la route (absent en création). */
  readonly id = input<string>();

  private readonly tickets = inject(TicketService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  protected readonly types = TYPES;
  protected readonly severites = SEVERITES;
  protected readonly libType = LIBELLES_TYPE;
  protected readonly libSeverite = LIBELLES_SEVERITE;

  protected readonly enCours = signal(false);
  protected readonly erreur = signal<string | null>(null);
  protected readonly erreursServeur = signal<Record<string, string>>({});

  // Mêmes règles que TicketRequest côté Spring Boot
  // Le même FormGroup sert à la création et à la modification ; en modification,
  // ngOnInit le remplit avec setValue() à partir du ticket chargé.
  protected readonly form = inject(NonNullableFormBuilder).group({
    titre: ['', [nonVide, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(4000)]],
    type: ['INCIDENT' as TypeTicket, [Validators.required]],
    severite: ['MOYENNE' as Severite, [Validators.required]],
  });

  // ngOnInit : hook appelé une fois, quand les input() (ici id) sont disponibles
  ngOnInit(): void {
    const id = this.id();
    if (id) {
      this.tickets.trouver(Number(id)).subscribe({
        next: (t) =>
          this.form.setValue({
            titre: t.titre,
            description: t.description ?? '',
            type: t.type,
            severite: t.severite,
          }),
        error: (err) => this.erreur.set(messageErreur(err)),
      });
    }
  }

  protected enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const request: TicketRequest = this.form.getRawValue();
    const id = this.id();
    // Opérateur ternaire : PUT si on a un id (modification), POST sinon (création)
    const appel = id ? this.tickets.modifier(Number(id), request) : this.tickets.creer(request);

    this.enCours.set(true);
    this.erreur.set(null);
    this.erreursServeur.set({});
    appel.subscribe({
      next: (ticket) => {
        this.notifications.succes(id ? 'Ticket modifié' : `Ticket #${ticket.id} créé`);
        void this.router.navigate(['/tickets', ticket.id]);
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
        this.erreursServeur.set(erreursParChamp(err));
      },
    });
  }
}
