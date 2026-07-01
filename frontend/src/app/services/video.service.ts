import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VideoDto } from '../models/video'; // Sale di un livello ed entra in models

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://192.168.1.30:8082/api/videos';
  private adminCourseUrl = 'http://192.168.1.30:8082/api/admin/courses';

  /**
   * Recupera il catalogo dei video gratuiti
   */
  getVideosPubblici(): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/pubblici`);
  }

  /**
   * Recupera tutti i video, inclusi i premium
   */
  getVideosPremium(): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/premium`);
  }

  /**
   * Permette allo STORE di salvare un nuovo video
   */
  salvaVideo(video: Partial<VideoDto>): Observable<VideoDto> {
    // L'URL punta all'endpoint base protetto da Spring Security
    return this.http.post<VideoDto>(`${this.apiUrl}`, video);
  }

  /**
   * Invia la richiesta di sincronizzazione della playlist al backend
   */
  syncPlaylist(playlistId: string): Observable<{ success: boolean; message: string }> {
    const params = new HttpParams().set('playlistId', playlistId);
    // Passiamo un oggetto vuoto {} come body perché l'endpoint richiede una POST
    return this.http.post<{ success: boolean; message: string }>(`${this.adminCourseUrl}`, {}, { params });
  }
}
















