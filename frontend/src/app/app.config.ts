import { ApplicationConfig, provideZonelessChangeDetection, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import { authInterceptor } from './security/auth-interceptor';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // 1. Attiva esplicitamente la modalità ultra-performante senza Zone.js (Standard Angular 22)
    provideZonelessChangeDetection(),

    // 2. Reindirizza gli errori non intercettati del browser verso l'ErrorHandler di Angular
    provideBrowserGlobalErrorListeners(),

    // 3. Gestisce le rotte dell'applicazione
    provideRouter(routes),

    // 4. Abilita il motore client per le chiamate HTTP verso Java Spring Boot
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};
