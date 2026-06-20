import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService, PostResponseDto, PostRequestDto } from '../../services/post';
import {CommentRequestDto, CommentService} from '../../services/comment';

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
  currentUserId: string = '';
  userRole: string = '';
  // Traccia l'ID del commento che si sta modificando (se null, nessun commento è in edit)
  editingCommentId: string | null = null;

  // Contiene i dati temporanei del commento che si sta modificando
  editCommentData: { content: string } = { content: '' };

  // Lista che conterrà i post recuperati dal DB
  postsList: PostResponseDto[] = [];

  // Modello per il form di creazione (usato solo se l'utente è STORE)
  newPostData: PostRequestDto = {
    title: '',
    content: '',
    isPrivate: false
  };

  successMessage: string | null = null;
  errorMessage: string | null = null;

  // Proprietà di supporto per la modifica
  editingPostId: string | null = null;
  editPostData: PostRequestDto = { title: '', content: '', isPrivate: false };
  commentInputs: { [postId: string]: string } = {};

  // 1. Attiva la modalità modifica sulla card del post
  onStartEdit(post: PostResponseDto): void {
    this.editingPostId = post.id;
    // Popolare form di modifica con i dati attuali del post
    this.editPostData = {
      title: post.title,
      content: post.content,
      isPrivate: post.isPrivate
    };
  }

  onStartEditComment(comment: any): void {
    this.editingCommentId = comment.id;
    this.editCommentData = { content: comment.content };
  }

  onCancelEditComment(): void {
    this.editingCommentId = null;
    this.editCommentData = { content: '' };
  }

  onSaveEditComment(postId: string, commentId: string): void {
    const testoPulito = this.editCommentData.content?.trim();

    if (!testoPulito) {
      return;
    }

    const payload: CommentRequestDto = {
      content: testoPulito,
      authorId: this.currentUserId  // Mantenere ID autore stateles
    };

    this.commentService.updateComment(postId, commentId, payload).subscribe({
      next: () => {
        this.editingCommentId = null;
        this.loadPosts();
    },
      error: (err) => {
        console.error('Errore durante la modifica del commento', err);
        this.errorMessage = 'Impossibile modificare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });

  }

  // 2. Annullare modifica
  onCancelEdit(): void {
    this.editingPostId = null;
    this.editPostData = {title: '', content: '', isPrivate: false};
}

  // 3. Inviare PUT al Server
  onSaveEdit(postId: string): void {
    const payload = {
      id: postId,
      title: this.editPostData.title,
      content: this.editPostData.content,
      isPrivate: this.editPostData.isPrivate || false,
      authorId: this.currentUserId,
      userRole: this.userRole
    };

    this.postService.updatePost(postId, payload).subscribe({
      next: () => {
        this.successMessage = 'Post aggiornato con successo';
        this.editingPostId = null;
        this.loadPosts();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore durante la modifica:', err);
        this.errorMessage = 'Impossibile modificare il post. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  ngOnInit(): void {
    // Recuperiamo i dati della sessione dal localStorage
    this.userEmail = localStorage.getItem('user_email');

    // Recupero ruolo (se null assegno strinag vuota di fallback)
    this.userRole = localStorage.getItem('user_role') || '';

    // ORA LEGGIAMO L'ID REALE DAL LOCALSTORAGE (senza usare più il valore fisso)
    this.currentUserId = localStorage.getItem('user_id') || '';

    // CODICE REALE IN PRODUZIONE:
    // Legge l'ID univoco assegnato a quel commerciante o studente dal database
    //this.currentUserId = localStorage.getItem('user_id') || 'e7470635-c6da-4474-941a-eb8cf9b6a072';

    // Per il momento id testo fisso preso da Postman
    // ci è utile in fase di sviluppo e/o collaudo
    //this.currentUserId = 'e7470635-c6da-4474-941a-eb8cf9b6a072';

    // Controllo di sicurezza base: se non ci sono dati, rimanda al login
    if (!this.userEmail || !this.currentUserId) {
      void this.router.navigate(['/login']);
    }

    // Appena la dashboard si carica, recuperiamo la lista dei post
    this.loadPosts();
  }

  loadPosts(): void {
    this.postService.getAllPosts(this.currentUserId, this.userRole).subscribe({
      next: (data) => {
        this.postsList = data;
        console.log('Post caricati con successo', this.postsList);

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
    // recupero valore booleano della checkbox del form
    const privacyInviata = this.newPostData.isPrivate;

    if (!titoloInviato || !contenutoInviato) {
      this.errorMessage = 'Titolo e contenuto sono obbligatori.';
      return;
    }

    const payload = {
      title: this.newPostData.title,
      content: this.newPostData.content,
      isPrivate: this.newPostData.isPrivate,
      authorId: this.currentUserId // L'ID dell'utente loggato
    };

    this.postService.createPost(payload).subscribe({
      next: () => {

        // Forza la pulizia dei vecchi residui
        this.errorMessage = null;
        this.successMessage = 'Post pubblicato con successo!';

        // Svuotiamo il form immediatamente
        this.newPostData = { title: '', content: '', isPrivate: false };

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

  onAddComment(postId: string): void {
    const content = this.commentInputs[postId];
    const testoPulito = content?.trim();

    // Se l'utente non ha scritto nulla, interrompiamo subito senza chiamare il server
    if(!testoPulito) {
      return;
    }

    this.commentService.createComment(postId, {
      content: testoPulito,
      authorId: this.currentUserId
    }).subscribe({
      next: () => {
        // Svuotare il campo di testo specifico di questo post
        this.commentInputs[postId] = '';

        // 1. Ordiniamo il ricaricamento dei post dal database
        this.loadPosts();
      },
      error: (err) => {
        console.error('Errore durante l\'invio del commento',err);
        this.errorMessage = 'Impossibile pubblicare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeleteComment(postId: string, commentId: string): void {
    // 1. Chiamiamo il metodo del servizio appena creato
    this.commentService.deleteComment(postId,commentId).subscribe({
      next: () => {
        // 2. Se l'eliminazione ha successo, aggiorno la bacheca
        this.loadPosts();
      },
      error: (err) => {
        console.log('Errore durante l\'eliminazione del commento', err);
        this.errorMessage = 'Impossibile eliminare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeletePost(postId: string): void {
    if (confirm('Sei sicuro di voler eliminare questo post e tutti i suoi commenti?')) {
      this.postService.deletePost(postId).subscribe({
        next: () => {
          // Ricarico la lista aggiornata da DB
          this.loadPosts();
        },
        error: (err) => {
          console.log('Errore durante l\' eliminazione del post:',err);
          this.errorMessage = 'Impossibile eliminare il post in questo momento.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onLogout(): void {
    // Svuotiamo la sessione e torniamo alla home
    localStorage.clear();
    void this.router.navigate(['/login']);
  }
}
