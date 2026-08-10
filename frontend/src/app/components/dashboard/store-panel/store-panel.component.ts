import { Component, Input, Output, EventEmitter, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService, PostRequestDto } from '../../../services/post';
import { VideoService } from '../../../services/video.service';
import { VideoDto } from '../../../models/video';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-store-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './store-panel.component.html'
})
export class StorePanelComponent {
  @Input({ required: true }) currentUserId!: string;
  @Output() postCreated = new EventEmitter<void>();
  @Output() videoCreated = new EventEmitter<void>();

  private postService = inject(PostService);
  private videoService = inject(VideoService);
  private cdr = inject(ChangeDetectorRef);

  successMessage: string | null = null;
  errorMessage: string | null = null;

  newPostData: PostRequestDto = {
    title: '',
    content: '',
    isPrivate: false
  };

  newVideoData: Partial<VideoDto> = {
    titolo: '',
    descrizione: '',
    youtubeId: '',
    premium: true
  };

  playlistIdInput: string = '';

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
        this.postCreated.emit();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('ERRORE RISPOSTA SERVER', err);
        this.successMessage = null;
        this.errorMessage = 'Errore durante la pubblicazione. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  onCreateVideo(event?: Event): void {
    if (event) event.preventDefault();

    if (!this.newVideoData.titolo || !this.newVideoData.youtubeId) {
      this.errorMessage = 'Titolo e ID YouTube sono obbligatori.';
      return;
    }

    this.videoService.salvaVideo(this.newVideoData).subscribe({
      next: (videoSalvato) => {
        this.successMessage = `Video "${videoSalvato.titolo}" aggiunto con successo!`;
        this.newVideoData = { titolo: '', descrizione: '', youtubeId: '', premium: true };
        this.videoCreated.emit();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore durante il salvataggio del video:', err);
        this.errorMessage = 'Impossibile salvare il video. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  onSyncPlaylist(event?: Event): void {
    if (event) event.preventDefault();

    const idPulito = this.playlistIdInput?.trim();
    if (!idPulito) {
      this.errorMessage = 'Inserisci un ID Playlist valido.';
      return;
    }

    this.videoService.syncPlaylist(idPulito).subscribe({
      next: (res) => {
        this.successMessage = res.message || 'Playlist sincronizzata con successo!';
        this.playlistIdInput = '';
        this.videoCreated.emit();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore durante la sincronizzazione:', err);
        this.errorMessage = 'Errore durante la sincronizzazione della playlist.';
        this.cdr.detectChanges();
      }
    });
  }
}
