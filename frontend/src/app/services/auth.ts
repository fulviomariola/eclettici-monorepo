import { Injectable, inject } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Observable } from 'rxjs';

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
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = 'http://192.168.1.30:8082/api/auth';

  registraUtente(dati: UserRegistrationDto): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, dati);
  }

  loginUtente(dati: UserRegistrationDto): Observable<LoginResponseDto> {
    // L'autenticazione HTTP Basic richiede le credenziali codificate in stringa btoa (Base64)
    // Formato richiesto dall'header: "Basic username:password"
/*    const credentials = btoa(`${dati.email}:${dati.password}`);
    const headers = new HttpHeaders({
      'Authorization': `Basic ${credentials}`
    })

    // Inviare richiesta POST passando l'header di autorizzazione
    return this.http.post<LoginResponseDto>(`${this.apiUrl}/login`, {}, { headers });*/

    // Inviamo direttamente i dati del form (email e password) nel Body della POST
    return this.http.post<LoginResponseDto>(`${this.apiUrl}/login`, dati);
  }
}





