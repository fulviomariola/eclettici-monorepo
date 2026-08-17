import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface BulkEmailRequest {
  target: 'ALL_SUBSCRIBERS' | 'ALL_LEADS' | 'SPECIFIC';
  recipientIds?: string[];
  subject: string;
  body: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmailService {
  private apiUrl = `${API_BASE_URL}/email`;

  constructor(private http: HttpClient) {}

  /**
   * Avvia l'invio massivo di email in background (Ruolo ADMIN)
   */
  sendBulkEmail(request: BulkEmailRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/bulk-send`, request, { responseType: 'text' });
  }
}
