import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

// DTO in uscita verso il server per creare un post (senza authorId, lo decide il backend!)
export interface PostRequestDto {
  title: string;
  content: string;
}

// 1. Aggiungiamo l'interfaccia dei commenti anche qui per il mapping
export interface CommentResponseDto {
  id: string;
  content: string;
  createdAt: string;
  authorId: string;
}

// 2. Aggiorniamo il DTO del Post includendo l'array dei commenti
export interface PostResponseDto {
  id: string;
  title: string;
  content: string;
  createdAt: string;
  authorId: string;
  comments: CommentResponseDto[];   // <--- lista commenti arrivata dal Backend!
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private http = inject(HttpClient);
  private apiUrl = 'http://192.168.1.30:8082/api/posts';

  // Cookie di sessione attivi nel browser (grazie a HttpBasic e CORS configurati)
  private httpOptions = {
    withCredentials: true
  };

  getAllPosts(): Observable<PostResponseDto[]> {
    return this.http.get<PostResponseDto[]>(this.apiUrl, this.httpOptions);
  }

  createPost(post: PostRequestDto): Observable<PostResponseDto> {
    return this.http.post<PostResponseDto>(this.apiUrl, post, { withCredentials: true });
  }

}
