import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);

  const userEmail = localStorage.getItem('user_email');

  if (userEmail) {
    return true;
  } else {
    void router.navigate(['/login']);  // operazione asincrona, ma non mi interessa aspettare il suo risultato prima di fare il return false
    return false;
  }
};
