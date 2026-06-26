import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { BehaviorSubject,Observable } from 'rxjs';

// DTO specifico e rigido per la Registrazione (tutti i campi sono obbligatori)
export interface UserRegistrationDto {
  nome?: string;
  cognome?: string;
  email: string;
  password:  string;
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
  private apiUrl = 'http://192.168.1.30:8082/api/auth';

  // Il BehaviorSubject mantiene lo stato del ruolo attuale (legge dal localStorage all'avvio)
  private userRoleSubject = new BehaviorSubject<string | null>(localStorage.getItem('user_role'));
  userRole$: Observable<string | null> = this.userRoleSubject.asObservable();

  /**
   * Metodo da chiamare subito dopo il login per aggiornare lo stato
   */
  aggiornaStatoSessione(): void {
    this.userRoleSubject.next(localStorage.getItem('user_role'));
  }

  /**
   * Svuota la sessione e notifica i componenti
   */
  logout(): void {
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
