import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';   // Adatta il percorso al progetto

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  const token = localStorage.getItem('token');
  const userEmail = localStorage.getItem('user_email');
  const userRole = localStorage.getItem('user_role');

  // 1. Controllo di sicurezza: se mancano i dati essenziali della sessione, rimanda al login
  if (!token || !userEmail) {
    authService.logout();
    void router.navigate(['/login']);
    return false;
  }

  // 2. Controllo validità temporale del Token JWT
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) {
      throw new Error('Formato token non valido');
    }

    // Decodifica il JSON contenuto nel payload del JWT
    const payload = JSON.parse(atob(payloadBase64));
    const isExpired = Date.now() >= payload.exp * 1000;

    if (isExpired) {
      authService.logout(); // Pulisce localStorage e azzera lo stato utente
      void router.navigate(['/login']);
      return false;
    }
  } catch (error) {
    // In caso di token corrotto o manomesso
    authService.logout();
    void router.navigate(['/login']);
    return false;
  }

  // 3. Controllo di autorizzazione: verifichiamo se la rotta ha dei ruoli specifici richiesti
  const expectedRoles = route.data['roles'] as string[] | undefined;
  if (expectedRoles && !expectedRoles.includes(userRole || '')) {
    void router.navigate(['/login']);
    return false;
  }

  return true;
};
