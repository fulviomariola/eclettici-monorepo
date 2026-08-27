import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';

// DTO specifico e rigido per la Registrazione (tutti i campi sono obbligatori)
export interface UserRegistrationDto {
  nome?: string;
  cognome?: string;
  email: string;
  password: string;
  ruolo?: string;
}

// DTO specifico e pulito per il Login
export interface LoginDto {
  email: string;
  password: string;
}

// Interfaccia per mappare la risposta del server in caso di login riuscito
export interface LoginResponseDto {
  id: string;
  email: string;
  role: string;
  messaggio: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = '/api/auth';

  // Timer per la gestione della scadenza automatica in background
  private timerScadenza: ReturnType<typeof setTimeout> | null = null;

  // Il BehaviorSubject mantiene lo stato del ruolo attuale (legge dal localStorage all'avvio)
  private userRoleSubject = new BehaviorSubject<string | null>(localStorage.getItem('user_role'));
  userRole$: Observable<string | null> = this.userRoleSubject.asObservable();

  constructor() {
    // Al ricaricamento della pagina, se c'è un token attivo avvia il monitoraggio della scadenza
    const token = localStorage.getItem('token');
    if (token) {
      this.avviaTimerAutoLogout(token);
    }
  }

  /**
   * Verifica se l'utente ha un token di sessione valido nel localStorage
   */
  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  /**
   * Ritorna il ruolo dell'utente corrente (es. 'USER', 'STORE', 'ADMIN')
   */
  getUserRole(): string | null {
    return localStorage.getItem('user_role');
  }

  /**
   * Ritorna l'identificativo UUID dell'utente corrente
   */
  getUserId(): string | null {
    return localStorage.getItem('user_id');
  }

  /**
   * Metodo da chiamare subito dopo il login per aggiornare lo stato e avviare il timer
   */
  aggiornaStatoSessione(): void {
    const token = localStorage.getItem('token');
    this.userRoleSubject.next(localStorage.getItem('user_role'));

    if (token) {
      this.avviaTimerAutoLogout(token);
    }
  }

  /**
   * Calcola i millisecondi rimanenti e programma il logout esatto
   */
  avviaTimerAutoLogout(token: string): void {
    if (this.timerScadenza) {
      clearTimeout(this.timerScadenza);
      this.timerScadenza = null;
    }

    try {
      const payloadBase64 = token.split('.')[1];
      if (!payloadBase64) {
        this.logout();
        return;
      }

      const payload = JSON.parse(atob(payloadBase64));
      const tempoRimanenteMs = (payload.exp * 1000) - Date.now();

      if (tempoRimanenteMs > 0) {
        this.timerScadenza = setTimeout(() => {
          this.logout();
          void this.router.navigate(['/login']);
        }, tempoRimanenteMs);
      } else {
        this.logout();
        void this.router.navigate(['/login']);
      }
    } catch {
      this.logout();
      void this.router.navigate(['/login']);
    }
  }

  /**
   * Svuota la sessione, interrompe i timer e notifica i componenti
   */
  logout(): void {
    if (this.timerScadenza) {
      clearTimeout(this.timerScadenza);
      this.timerScadenza = null;
    }

    localStorage.removeItem('token');
    localStorage.removeItem('user_id');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_role');
    this.userRoleSubject.next(null);
  }

  registraUtente(dati: UserRegistrationDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, dati);
  }

  loginUtente(dati: LoginDto): Observable<LoginResponseDto> {
    return this.http.post<LoginResponseDto>(`${this.apiUrl}/login`, dati);
  }

  /**
   * Verifica se l'email esiste già nel database
   */
  checkEmail(email: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check-email`, {
      params: { email }
    });
  }
}
