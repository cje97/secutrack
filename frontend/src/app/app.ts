import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth.service';

/**
 * Composant racine : la "coquille" de l'application (barre d'outils + zone où s'affichent les pages).
 */
// @Component : déclare un composant Angular.
//   selector    : balise HTML qui l'affiche (<app-root> dans index.html)
//   imports     : composants et directives utilisés dans le template (composant "standalone")
//   templateUrl : fichier HTML du composant ; styleUrl : son CSS (limité à ce composant)
@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  // inject() : injection de dépendance (comme le constructeur Spring). protected : utilisable dans le template.
  protected readonly auth = inject(AuthService);
}
