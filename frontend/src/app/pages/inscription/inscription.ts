import { Component, inject, signal } from '@angular/core';
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
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { erreursParChamp, messageErreur } from '../../core/erreur-api';
import { NotificationService } from '../../core/notification.service';

/**
 * Validateur de GROUPE : il reçoit le FormGroup entier (et pas un seul champ),
 * ce qui permet de comparer deux champs entre eux. Renvoie null si tout va bien.
 */
export function motsDePasseIdentiques(groupe: AbstractControl): ValidationErrors | null {
  const mdp = groupe.get('motDePasse')?.value;
  const confirmation = groupe.get('confirmation')?.value;
  return mdp === confirmation ? null : { differents: true };
}

/**
 * Page d'inscription : crée un compte avec le rôle USER, puis connecte directement l'utilisateur.
 */
@Component({
  selector: 'app-inscription',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './inscription.html',
  styleUrl: '../auth-pages.scss',
})
export class Inscription {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notifications = inject(NotificationService);

  protected readonly enCours = signal(false);
  protected readonly erreur = signal<string | null>(null);
  // Erreurs renvoyées par Spring champ par champ (clé "erreurs" du ProblemDetail, ex. courriel déjà utilisé)
  protected readonly erreursServeur = signal<Record<string, string>>({});

  // Mêmes règles que RegisterRequest côté Spring Boot
  protected readonly form = inject(NonNullableFormBuilder).group(
    {
      nomComplet: ['', [Validators.required, Validators.maxLength(100)]],
      courriel: ['', [Validators.required, Validators.email]],
      motDePasse: [
        '',
        [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(72),
          Validators.pattern(/.*\d.*/),
        ],
      ],
      confirmation: ['', [Validators.required]],
    },
    // 2e argument de group() : validateurs qui portent sur tout le groupe
    { validators: motsDePasseIdentiques },
  );

  protected inscrire(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // Déstructuration : on n'envoie PAS le champ "confirmation", que l'API ne connaît pas
    const { nomComplet, courriel, motDePasse } = this.form.getRawValue();
    this.enCours.set(true);
    this.erreur.set(null);
    this.erreursServeur.set({});
    this.auth.inscrire({ nomComplet, courriel, motDePasse }).subscribe({
      next: () => {
        this.notifications.succes('Compte créé. Bienvenue !');
        void this.router.navigate(['/']);
      },
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(messageErreur(err));
        this.erreursServeur.set(erreursParChamp(err));
      },
    });
  }
}
