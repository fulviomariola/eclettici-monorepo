import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export type ContactMessageStatus = 'NEW' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED';

export interface ContactMessage {
  id: string;
  name: string;
  companyName?: string;
  email: string;
  phone?: string;
  message: string;
  createdAt: string;
  status: ContactMessageStatus;
}

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private apiUrl = `${API_BASE_URL}/contacts`;

  constructor(private http: HttpClient) {}

  /**
   * Recupera tutti i messaggi di contatto (Richiede ruolo STORE o ADMIN)
   */
  getAllMessages(): Observable<ContactMessage[]> {
    return this.http.get<ContactMessage[]>(this.apiUrl);
  }

  /**
   * Aggiorna lo stato di un messaggio (es. NEW -> IN_PROGRESS)
   */
  updateStatus(id: string, status: ContactMessageStatus): Observable<ContactMessage> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ContactMessage>(`${this.apiUrl}/${id}/status`, {}, { params });
  }
}
