import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// DTO in ingresso: inviamo solo il contenuto text, l'autore lo decide il backend!
export interface CommentRequestDto {
  content: string;
}

// DTO in uscita: la risposta che arriva dal DB con tutti gli ID separati
export interface CommentResponseDto {
  id: string;
  content: string;
  createdAt: string;
  postId: string;
  authorId: string;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private http = inject(HttpClient);

  // Funzione per generare l'URL dinamico basato sul Post specifico
  private getApiUrl(postId: string): string {
    return `http://192.168.1.30:8082/api/posts/${postId}/comments`;
  }

  // Abilitiamo il passaggio dei cookie di sessione per l'autenticazione HTTP Basic
  private httpOptions = {
    withCredentials: true
  };

  /**
   * Invia un nuovo commento associato a un post specifico
   */
  createComment(postId: string, comment: CommentRequestDto): Observable<CommentResponseDto> {
    return this.http.post<CommentResponseDto>(this.getApiUrl(postId), comment, this.httpOptions);
  }
}
