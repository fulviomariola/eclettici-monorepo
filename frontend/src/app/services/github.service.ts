import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RepositoryDto } from '../models/repository';

@Injectable({
  providedIn: 'root'
})
export class GithubService {
  private http = inject(HttpClient);

  // Sostituisci 'tuo-username-github' con il tuo vero username di GitHub
  private username = 'fulviomariola';
  private apiUrl = `https://api.github.com/users/${this.username}/repos`;

  /**
   * Recupera la lista dei repository pubblici direttamente da GitHub
   */
  getRepositories(): Observable<RepositoryDto[]> {
    // Ordiniamo i repository per ultimo aggiornamento (pushed)
    return this.http.get<RepositoryDto[]>(this.apiUrl, {
      params: { sort: 'pushed', per_page: '6' }
    });
  }
}
