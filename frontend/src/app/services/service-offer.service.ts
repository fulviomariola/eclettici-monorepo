import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface ServiceOffer {
  id?: string;
  title: string;
  description: string;
  iconName?: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceOfferService {
  private apiUrl = `${API_BASE_URL}/services`;

  constructor(private http: HttpClient) {}

  /**
   * Recupera i servizi attivi per la vetrina pubblica
   */
  getPublicServices(): Observable<ServiceOffer[]> {
    return this.http.get<ServiceOffer[]>(this.apiUrl);
  }

  /**
   * Crea un nuovo servizio (Richiede ruolo STORE o ADMIN)
   */
  createService(service: ServiceOffer): Observable<ServiceOffer> {
    return this.http.post<ServiceOffer>(this.apiUrl, service);
  }

  /**
   * Aggiorna un servizio esistente (Richiede ruolo STORE o ADMIN)
   */
  updateService(id: string, service: ServiceOffer): Observable<ServiceOffer> {
    return this.http.put<ServiceOffer>(`${this.apiUrl}/${id}`, service);
  }

  /**
   * Elimina un servizio dal database (Richiede ruolo STORE o ADMIN)
   */
  deleteService(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
