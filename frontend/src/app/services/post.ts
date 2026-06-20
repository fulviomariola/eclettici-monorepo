import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// DTO in uscita verso il server per creare un post (senza authorId, lo decide il backend!)
export interface PostRequestDto {
  title: string;
  content: string;
  isPrivate: boolean;
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
  isPremium: boolean;
  isPrivate: boolean;
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

  getAllPosts(currentUserId: string, userRole: string): Observable<PostResponseDto[]> {
    // Costruire Query Params da appendere all'URL
    const params = new HttpParams()
      .set('currentUserId', currentUserId)
      .set('userRole', userRole);

    // passo parametri dentro oggetto di config insieme a withCredentials
    return this.http.get<PostResponseDto[]>(this.apiUrl, {
      ...this.httpOptions,
      params: params
    });
  }

  createPost(post: PostRequestDto): Observable<PostResponseDto> {
    return this.http.post<PostResponseDto>(this.apiUrl, post, { withCredentials: true });
  }

  updatePost(postId: string, postData: PostRequestDto, currentUserId: string): Observable<PostResponseDto> {
    const params = new HttpParams().set('currentUserId',currentUserId);

    return this.http.put<PostResponseDto>(`${this.apiUrl}/${postId}`, postData, {
      ...this.httpOptions,
      params: params
    });
  }

  deletePost(postId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${postId}`, this.httpOptions);
  }

  addComment(postId: string, content: string): Observable<any> {
    // Backend si aspetta un CommentRequestDto. Creiamo oggeto con campo 'content'
    const body = { content: content };

    return this.http.post(`http://192.168.1.30:8082/api/post/${postId}/comments`, body, this.httpOptions);
  }
}
