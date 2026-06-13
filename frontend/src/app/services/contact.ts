import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Contratto dati allineato al tuo ContactRequestDto in Java
export interface ContactRequestDto {
  name: string;
  companyName?: string;
  email: string;
  phone?: string;
  message: string;
}

@Injectable({
  providedIn: 'root' // Rende il servizio disponibile in tutta l'applicazione (Singleton)
})
export class ContactService {
  // Iniezione del modulo di rete
  private http = inject(HttpClient);

  // Endpoint del tuo backend Spring Boot
  private apiUrl = 'http://localhost:8082/api/contacts';

  /**
   * Invia il payload JSON al controller Java
   */
  inviaRichiesta(payload: ContactRequestDto): Observable<any> {
    return this.http.post<any>(this.apiUrl, payload);
  }
}
