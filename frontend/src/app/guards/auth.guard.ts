import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  // Verifichiamo se l'email dell'utente è presente nel localStorage
  const userEmail = localStorage.getItem('user_email');

  if (userEmail) {
    // Utente loggato: semaforo VERDE, può passare
    return true;
  } {
    // Utente NON loggato: semaforo ROSSO, lo rimbalziamo al login
    router.navigate(['/login']);
    return false;
  }
};
