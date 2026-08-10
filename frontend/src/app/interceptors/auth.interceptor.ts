import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('token'); // Se usi una chiave diversa (es. 'jwt_token'), aggiornala qui

  // 1. Inietta automaticamente l'header Authorization se il token è presente
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // 2. Intercetta gli errori HTTP in uscita
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se il token è scaduto o non valido (401 Unauthorized)
      if (error.status === 401) {
        localStorage.clear();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
