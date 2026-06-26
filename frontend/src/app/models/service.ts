import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VideoDto } from '../models/video';

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://192.168.1.30:8082/api/videos'; // Endpoint del tuo backend Spring

  /**
   * Recupera il catalogo dei video gratuiti (per utenti anonimi e STUDENT)
   */
  getVideosPubblici(): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/pubblici`);
  }

  /**
   * Recupera tutti i video, inclusi i premium (riservato a chi ha i permessi)
   */
  getVideosPremium(): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/premium`);
  }

  /**
   * Permette allo STORE (Tu) di salvare un nuovo video importato da YouTube
   */
  salvaVideo(video: Partial<VideoDto>): Observable<VideoDto> {
    return this.http.post<VideoDto>(this.apiUrl, video);
  }
}
