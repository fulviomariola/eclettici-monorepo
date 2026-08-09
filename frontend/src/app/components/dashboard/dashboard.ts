import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService, PostResponseDto, PostRequestDto } from '../../services/post';
import { CommentRequestDto, CommentService } from '../../services/comment';
import { ScrollFadeDirective } from '../../directives/scroll-fade.directive';

// IMPORT DEI VIDEO E DELLA PIPE
import { VideoService } from '../../services/video.service';
import { VideoDto } from '../../models/video';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective, SafeUrlPipe], // AGGIUNTO SafeUrlPipe QUI
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private router = inject(Router);
  private postService = inject(PostService);
  private cdr = inject(ChangeDetectorRef);
  private commentService = inject(CommentService);
  private videoService = inject(VideoService); // INIETTATO SERVIZIO VIDEO

  userEmail: string | null = '';
  currentUserId: string = '';
  userRole: string = '';

  // Lista per i video dell'Academy
  videosList: VideoDto[] = [];

  editingCommentId: string | null = null;
  editCommentData: { content: string } = { content: '' };
  postsList: PostResponseDto[] = [];

  newPostData: PostRequestDto = {
    title: '',
    content: '',
    isPrivate: false
  };

  successMessage: string | null = null;
  errorMessage: string | null = null;

  editingPostId: string | null = null;
  editPostData: PostRequestDto = { title: '', content: '', isPrivate: false };
  commentInputs: { [postId: string]: string } = {};

  ngOnInit(): void {
    this.userEmail = localStorage.getItem('user_email');
    this.userRole = localStorage.getItem('user_role') || '';
    this.currentUserId = localStorage.getItem('user_id') || '';

    if (!this.userEmail || !this.currentUserId) {
      void this.router.navigate(['/login']);
    }

    this.loadPosts();
    this.loadVideos(); // CARICHIAMO I VIDEO ALL'AVVIO
  }

  loadVideos(): void {
    // Se l'utente è STUDENT o STORE/ADMIN, carica la lista dei video premium
    this.videoService.getVideosPremium().subscribe({
      next: (data) => {
        this.videosList = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei video:', err);
      }
    });
  }

  // --- RESTO DEL TUO CODICE PER POST E COMMENTI INALTERATO ---

  onStartEdit(post: PostResponseDto): void {
    this.editingPostId = post.id;
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
    if (!testoPulito) return;

    const payload: CommentRequestDto = {
      content: testoPulito,
      authorId: this.currentUserId
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

  onCancelEdit(): void {
    this.editingPostId = null;
    this.editPostData = {title: '', content: '', isPrivate: false};
  }

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

  loadPosts(): void {
    this.postService.getAllPosts(this.currentUserId, this.userRole).subscribe({
      next: (data) => {
        this.postsList = data;
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
    if (event) event.preventDefault();

    const titoloInviato = this.newPostData.title?.trim();
    const contenutoInviato = this.newPostData.content?.trim();

    if (!titoloInviato || !contenutoInviato) {
      this.errorMessage = 'Titolo e contenuto sono obbligatori.';
      return;
    }

    const payload = {
      title: this.newPostData.title,
      content: this.newPostData.content,
      isPrivate: this.newPostData.isPrivate,
      authorId: this.currentUserId
    };

    this.postService.createPost(payload).subscribe({
      next: () => {
        this.errorMessage = null;
        this.successMessage = 'Post pubblicato con successo!';
        this.newPostData = { title: '', content: '', isPrivate: false };
        this.loadPosts();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 8000);
      },
      error: (err) => {
        console.error('ERRORE RISPOSTA SERVER', err);
        this.successMessage = null;
        this.errorMessage = 'Errore durante la pubblicazione. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  onAddComment(postId: string): void {
    const content = this.commentInputs[postId];
    const testoPulito = content?.trim();

    if(!testoPulito) return;

    this.commentService.createComment(postId, {
      content: testoPulito,
      authorId: this.currentUserId
    }).subscribe({
      next: () => {
        this.commentInputs[postId] = '';
        this.loadPosts();
      },
      error: (err) => {
        console.error('Errore durante l\'invio del commento', err);
        this.errorMessage = 'Impossibile pubblicare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeleteComment(postId: string, commentId: string): void {
    this.commentService.deleteComment(postId, commentId).subscribe({
      next: () => {
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
          this.loadPosts();
        },
        error: (err) => {
          console.log('Errore durante l\'eliminazione del post:', err);
          this.errorMessage = 'Impossibile eliminare il post in questo momento.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onLogout(): void {
    localStorage.clear();
    void this.router.navigate(['/login']);
  }
}
