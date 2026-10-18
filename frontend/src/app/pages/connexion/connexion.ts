import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { messageErreur } from '../../core/erreur-api';

/**
 * Page de connexion. Formulaire réactif (Reactive Forms) : la structure et les règles de validation
 * sont déclarées en TypeScript, le template HTML ne fait que s'y relier (formGroup, formControlName).
 */
@Component({
  selector: 'app-connexion',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './connexion.html',
  styleUrl: '../auth-pages.scss',
})
export class Connexion {
  /** ?retour=/tickets/3 : page à rouvrir après la connexion (posé par authGuard). */
  // input() : propriété reçue de l'extérieur ; ici, remplie par le routeur depuis l'URL
  readonly retour = input<string>();

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enCours = signal(false);
  protected readonly erreur = signal<string | null>(null);
  protected readonly masquerMotDePasse = signal(true);

  // FormGroup : un objet par champ, avec sa valeur initiale et ses validateurs.
  // NonNullable : reset() remet la valeur initiale ('') au lieu de null.
  protected readonly form = inject(NonNullableFormBuilder).group({
    courriel: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required]],
  });

  /** Raccourci pour tester rapidement les deux rôles avec les comptes de démonstration. */
  protected remplirDemo(courriel: string): void {
    this.form.setValue({ courriel, motDePasse: 'Demo1234!' });
  }

  protected connecter(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enCours.set(true);
    this.erreur.set(null);
    // subscribe() déclenche réellement la requête HTTP ; next = succès, error = échec
    this.auth.connecter(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl(this.retour() || '/'),
      error: (err) => {
        this.enCours.set(false);
        this.erreur.set(
          err?.status === 401 ? 'Courriel ou mot de passe invalide.' : messageErreur(err),
        );
      },
    });
  }
}
