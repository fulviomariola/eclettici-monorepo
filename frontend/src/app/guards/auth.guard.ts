import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  const token = localStorage.getItem('token');
  const userEmail = localStorage.getItem('user_email');
  const userRole = localStorage.getItem('user_role');

  // 1. Controllo di sicurezza: se mancano i dati essenziali della sessione, rimanda al login
  if (!token || !userEmail) {
    void router.navigate(['/login']);
    return false;
  }

  // 2. Controllo di autorizzazione: verifichiamo se la rotta ha dei ruoli specifici richiesti
  const expectedRoles = route.data['roles'] as string[] | undefined;

  // Se la rotta richiede ruoli specifici e l'utente corrente non li possiede
  if (expectedRoles && !expectedRoles.includes(userRole || '')) {
    void router.navigate(['/login']);
    return false;
  }

  // Se passa tutti i controlli, la rotta viene sbloccata
  return true;
};
