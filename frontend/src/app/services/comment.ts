import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentRequestDto, CommentResponseDto } from '../models/comment';

// RISOLUZIONE CRITICITÀ TS1205: Esplicitiamo 'type' per la compatibilità con isolatedModules
export type { CommentRequestDto, CommentResponseDto };

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://192.168.1.30:8082/api/comments';

  private httpOptions = {
    withCredentials: true
  };

  /**
   * Recupera tutti i commenti associati a una specifica videolezione.
   * URL: GET /api/comments/video/{videoId}
   */
  getCommentiPerVideo(videoId: number): Observable<CommentResponseDto[]> {
    return this.http.get<CommentResponseDto[]>(`${this.apiUrl}/video/${videoId}`, this.httpOptions);
  }

  /**
   * Recupera tutti i commenti associati a un determinato Post della Dashboard.
   * URL: GET /api/comments/post/{postId}
   */
  getCommentiPerPost(postId: string): Observable<CommentResponseDto[]> {
    return this.http.get<CommentResponseDto[]>(`${this.apiUrl}/post/${postId}`, this.httpOptions);
  }

  /**
   * Invia un nuovo commento.
   * Gestisce sia la chiamata a 2 argomenti (Dashboard) sia quella a 1 argomento (Videolezioni).
   */
  createComment(postIdOrComment: string | CommentRequestDto, comment?: CommentRequestDto): Observable<CommentResponseDto> {
    if (typeof postIdOrComment === 'string') {
      // Caso Dashboard: mappa il postId all'interno del DTO prima di inviare al backend unico
      const payload = { ...comment, postId: postIdOrComment };
      return this.http.post<CommentResponseDto>(this.apiUrl, payload, this.httpOptions);
    } else {
      // Caso Videolezioni: invia direttamente il payload già strutturato
      return this.http.post<CommentResponseDto>(this.apiUrl, postIdOrComment, this.httpOptions);
    }
  }

  /**
   * Modifica un commento esistente.
   * Mantiene i 3 argomenti per non rompere la Dashboard, ignorando internamente il postId non più richiesto dal controller.
   */
  updateComment(postId: string, commentId: string, comment: CommentRequestDto): Observable<CommentResponseDto> {
    return this.http.put<CommentResponseDto>(`${this.apiUrl}/${commentId}`, comment, this.httpOptions);
  }

  /**
   * Cancella un commento.
   * Mantiene i 2 argomenti per non rompere la Dashboard, ignorando internamente il postId.
   */
  deleteComment(postId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${commentId}`, this.httpOptions);
  }
}
