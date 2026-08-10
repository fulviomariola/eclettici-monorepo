import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class ProgressService {

  private apiUrl = `${API_BASE_URL}/progress`;

  constructor(private http: HttpClient) { }

  /**
   * Aggiorna lo stato di completamento di un video per l'utente loggato.
   */
  aggiornaProgresso(videoId: number, completato: boolean): Observable<any> {
    // Genera l'URL includendo il parametro ?completato=true/false
    return this.http.post<any>(`${this.apiUrl}/video/${videoId}?completato=${completato}`, {});
  }

  /**
   * Recupera lo stato di completamento di un singolo video per l'utente loggato.
   */
  getProgressoVideo(videoId: number): Observable<{ isCompleted: boolean }> {
    return this.http.get<{ isCompleted: boolean }>(`${this.apiUrl}/video/${videoId}`);
  }

  /**
   * Recupera la percentuale totale di avanzamento del corso.
   */
  getPercentualeAvanzamento(): Observable<{ percentuale: number }> {
    return this.http.get<{ percentuale: number }>(`${this.apiUrl}/percentuale`);
  }
}
