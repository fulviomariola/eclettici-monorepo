import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService, PostResponseDto, PostRequestDto } from '../../services/post';
import { CommentService } from '../../services/comment';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private router = inject(Router);
  private postService = inject(PostService);
  private  cdr = inject(ChangeDetectorRef);
  private commentService = inject(CommentService); // INIETTARE IL SERVIZIO

  userEmail: string | null = '';
  userRole: string | null = '';

  // Lista che conterrà i post recuperati dal DB
  postsList: PostResponseDto[] = [];

  // Modello per il form di creazione (usato solo se l'utente è STORE)
  newPostData: PostRequestDto = {
    title: '',
    content: ''
  };

  successMessage: string | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    // Recuperiamo i dati della sessione dal localStorage
    this.userEmail = localStorage.getItem('user_email');
    this.userRole = localStorage.getItem('user_role');

    // Controllo di sicurezza base: se non ci sono dati, rimanda al login
    if (!this.userEmail) {
      void this.router.navigate(['/login']);
    }

    // Appena la dashboard si carica, recuperiamo la lista dei post
    this.loadPosts();
  }

  loadPosts(): void {
    this.postService.getAllPosts().subscribe({
      next: (posts) => {
        this.postsList = posts;

        // Svegliamo il motore grafico di Angular non appena i post arrivano dal backend!
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei post', err);
        this.errorMessage = 'Impossibile caricare i post del blog.';
        this.cdr.detectChanges();
      }
    });
  }

  onCreatePost(event?: Event): void {
    // Blocca immediatamente il comportamento di sottomissione nativo del browser
    if (event) {
      event.preventDefault();
    }

    // Estraiamo i valori per evitare letture asincrone sfasate
    const titoloInviato = this.newPostData.title?.trim();
    const contenutoInviato = this.newPostData.content?.trim();

    if (!titoloInviato || !contenutoInviato) {
      this.errorMessage = 'Titolo e contenuto sono obbligatori.';
      return;
    }

    // Prepariamo il payload pulito da inviare
    const payload: PostRequestDto = {
      title: titoloInviato,
      content: contenutoInviato
    };

    this.postService.createPost(payload).subscribe({
      next: (savedPost) => {

        // Forza la pulizia dei vecchi residui
        this.errorMessage = null;
        this.successMessage = 'Post pubblicato con successo!';

        // Svuotiamo il form immediatamente
        this.newPostData = { title: '', content: '' };

        // Ricarichiamo la bacheca
        this.loadPosts();

        // Rifresca lo schermmo subito!
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
          }, 8000);
      },
      error: (err) => {
        console.error('ERROPRE RISPOSTA SERVER', err);
        this.successMessage = null;
        this.errorMessage = 'Errore durante la pubblicazione. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  onAddComment(postId: string, content: string): void {
    const testoPulito = content?.trim();

    // Se l'utente non ha scritto nulla, interrompiamo subito senza chiamare il server
    if(!testoPulito) {
      return;
    }

    this.commentService.createComment(postId, { content: testoPulito }).subscribe({
      next: (savedComment) => {
        // Trucco strategico: ricarichiamo tutti i post.
        // Poiché il backend ora include i commenti in "getAllPosts", la bacheca si aggiornerà da sola!
        this.loadPosts();
      },
      error: (err) => {
        console.error('Errore durante l\'invio del commento',err);
        this.errorMessage = 'Impossibile pubblicare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });

  }

  onLogout(): void {
    // Svuotiamo la sessione e torniamo alla home
    localStorage.clear();
    void this.router.navigate(['/login']);
  }
}
