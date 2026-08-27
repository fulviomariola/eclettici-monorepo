import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoService } from '../../services/video.service';
import { VideoDto } from '../../models/video';

@Component({
  selector: 'app-gestione-video',
  templateUrl: './gestione-video.html',
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class GestioneVideoComponent {
  private videoService = inject(VideoService);
  private cdr = inject(ChangeDetectorRef);

  playlistIdInput: string = '';
  isSubmitting: boolean = false;
  feedbackMessage: string = '';
  isError: boolean = false;

  onSyncPlaylist(): void {
    let rawInput = this.playlistIdInput.trim();
    if (!rawInput) {
      this.showFeedback('Inserisci un ID o URL playlist valido.', true);
      return;
    }

    // Estrae automaticamente l'ID se l'utente incolla l'URL completo di YouTube
    const match = rawInput.match(/[?&]list=([^#&?]+)/);
    const cleanPlaylistId = match ? match[1] : rawInput;

    this.isSubmitting = true;
    this.feedbackMessage = "Sincronizzazione in corso... Attendere.";
    this.isError = false;
    this.cdr.detectChanges();

    this.videoService.syncPlaylist(cleanPlaylistId).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.showFeedback(response.message || 'Playlist sincronizzata con successo!', false);
        this.playlistIdInput = '';
      },
      error: (err) => {
        this.isSubmitting = false;
        const errorMessage = err.error?.message || (typeof err.error === 'string' ? err.error : `Errore di sincronizzazione (${err.status})`);
        this.showFeedback(errorMessage, true);
      }
    });
  }

/*  onSyncPlaylist(): void {
    if (!this.playlistIdInput.trim()) {
      this.showFeedback('Inserisci un ID o URL playlist valido.', true);
      return;
    }

    this.isSubmitting = true;
    this.feedbackMessage = "Sincronizzazione in corso... Attendere.";
    this.isError = false;
    this.cdr.detectChanges();

    this.videoService.syncPlaylist(this.playlistIdInput.trim()).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.showFeedback(response.message || 'Playlist sincronizzata con successo!', false);
        this.playlistIdInput = '';  // Svuota l'input
      },
      error: (err) => {
        this.isSubmitting = false;
        // Recupera il messaggio di errore dal backend o genera un messaggio leggibile
        const errorMessage = err.error?.message || (typeof err.error === 'string' ? err.error : `Errore di sincronizzazione (${err.status})`);
        this.showFeedback(errorMessage, true);
      }
    });
  }*/

  // Modello legato al Form di inserimento manuale singolo video
  nuovoVideo: VideoDto = {
    titolo: '',
    descrizione: '',
    youtubeId: '',
    thumbnailUrl: '',
    premium: false
  };

  messaggioSuccesso: string | null = null;
  messaggioErrore: string | null = null;
  inviando: boolean = false;

  inviaForm(): void {
    this.messaggioSuccesso = null;
    this.messaggioErrore = null;
    this.inviando = true;

    this.nuovoVideo.premium = !!this.nuovoVideo.premium;

    if (!this.nuovoVideo.thumbnailUrl.trim() && this.nuovoVideo.youtubeId) {
      this.nuovoVideo.thumbnailUrl = `https://img.youtube.com/vi/${this.nuovoVideo.youtubeId}/hqdefault.jpg`;
    }

    this.videoService.salvaVideo(this.nuovoVideo).subscribe({
      next: (videoSalvato) => {
        this.messaggioSuccesso = `Video "${videoSalvato.titolo}" inserito con successo nel database!`;
        this.resetForm();
        this.inviando = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio:", err);
        this.messaggioErrore = "Impossibile salvare il video. Verifica token e permessi STORE/ADMIN.";
        this.inviando = false;
        this.cdr.detectChanges();
      }
    });
  }

  private showFeedback(message: string, error: boolean): void {
    this.feedbackMessage = message;
    this.isError = error;
    this.cdr.detectChanges(); // <-- Forza sempre l'aggiornamento grafico
  }

  private resetForm(): void {
    this.nuovoVideo = {
      titolo: '',
      descrizione: '',
      youtubeId: '',
      thumbnailUrl: '',
      premium: false
    };
  }
}
