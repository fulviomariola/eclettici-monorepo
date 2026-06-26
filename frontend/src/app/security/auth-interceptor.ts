import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // recupero passaporto digitale di localStorage
  const token = localStorage.getItem('token');

  // AGGIUNGI QUESTO CONTROLLO: Clona solo se la richiesta è diretta al tuo backend
  // (Evita di inviare il token a api.github.com)
  const isRichiestaBackend = req.url.includes('192.168.1.30') || req.url.includes('/api/');

  // se token esiste, clono richiesta originale e incolliamo header di sicurezza
  if(token && isRichiestaBackend) {
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
