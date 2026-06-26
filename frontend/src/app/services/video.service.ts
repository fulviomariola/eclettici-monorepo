import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { VideoDto } from '../models/video'; // Sale di un livello ed entra in models

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  private http = inject(HttpClient);
  private apiUrl = 'http://192.168.1.30:8082/api/videos';

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
    return this.http.post<VideoDto>(this.apiUrl, video);
  }
}
