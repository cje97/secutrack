import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { MAT_ICON_DEFAULT_OPTIONS } from '@angular/material/icon';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

/**
 * Configuration globale de l'application (l'équivalent des @Bean de Spring côté Angular).
 * Chaque "provider" rend un service disponible pour toute l'application.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    // Routage. withComponentInputBinding() : les paramètres d'URL (:id, ?retour=)
    // arrivent directement dans les input() des composants
    provideRouter(routes, withComponentInputBinding()),
    // HttpClient pour appeler l'API. withInterceptors : chaque requête passe par authInterceptor,
    // qui ajoute le JWT (équivalent d'un filtre côté Spring)
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),
    // <mat-icon> utilise la police Material Symbols (hébergée localement, voir angular.json)
    { provide: MAT_ICON_DEFAULT_OPTIONS, useValue: { fontSet: 'material-symbols-outlined' } },
  ],
};
