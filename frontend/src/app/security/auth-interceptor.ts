import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // recupero passaporto digitale di localStorage
  const token = localStorage.getItem('token');

  // se token esiste, clono richiesta originale e incolliamo header di sicurezza
  if(token) {
    const reqClonata = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    // Inviamo la richiesta modificata al casellante successivo
    return next(reqClonata);
  }

  // Se non c'è il token (es. durante il login o la registrazione), la richiesta parte senza modifiche
  return next(req);
};
