import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { VideoDto } from '../models/video';

@Injectable({
  providedIn: 'root'
})
export class VideoService {
  private http = inject(HttpClient);

  private apiUrl = `${API_BASE_URL}/videos`;
  private adminCourseUrl = `${API_BASE_URL}/admin/courses/sync`;

  getVideosPubblici(): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/pubblici`);
  }

  getVideosPremium(): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/premium`);
  }

  salvaVideo(video: Partial<VideoDto>): Observable<VideoDto> {
    return this.http.post<VideoDto>(this.apiUrl, video);
  }

  syncPlaylist(playlistId: string): Observable<{ success: boolean; message: string; courseId?: number }> {
    const params = new HttpParams().set('playlistId', playlistId);
    return this.http.post<{ success: boolean; message: string; courseId?: number }>(
      this.adminCourseUrl,
      {},
      { params }
    );
  }
}
