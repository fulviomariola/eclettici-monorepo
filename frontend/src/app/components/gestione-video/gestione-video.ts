import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Fondamentale per il databinding bivalente [(ngModel)]
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
    if (!this.playlistIdInput.trim()) {
      this.showFeedback('Inserisci un ID playlist valido.', true);
      return;
    }

    this.isSubmitting = true;
    this.feedbackMessage = "Sincronizzazione in corso... Attendere.";
    this.isError = false;

    this.videoService.syncPlaylist(this.playlistIdInput.trim()).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        this.showFeedback(response.message, false);
        this.playlistIdInput = '';  // Svuota l'input in caso di successo
        this.cdr.detectChanges();

      },
      error: (err) => {
        this.isSubmitting = false;
        // Recupera il messaggio d'errore inviato dalla mappa del backend
        const errorMessage = err.error?.message || `Errore di rete o permessi insufficienti (${err.status})`;
        this.showFeedback(errorMessage, true);
      }
    });
  }

  // Modello vuoto legato ai campi del Form
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

    // FORZATURA: Se premium è null o undefined, lo trasformiamo in un false nativo
    this.nuovoVideo.premium = !!this.nuovoVideo.premium;

    // Se l'utente non inserisce una thumbnail, ne generiamo una standard partendo dall'ID YouTube
    if (!this.nuovoVideo.thumbnailUrl.trim() && this.nuovoVideo.youtubeId) {
      this.nuovoVideo.thumbnailUrl = `https://img.youtube.com/vi/${this.nuovoVideo.youtubeId}/0.jpg`;
    }

    this.videoService.salvaVideo(this.nuovoVideo).subscribe({
      next: (videoSalvato) => {
        this.messaggioSuccesso = `Video "${videoSalvato.titolo}" inserito con successo nel database!`;
        this.resetForm();
        this.inviando = false;
        this.cdr.detectChanges(); // Svegliamo Angular per mostrare il banner verde
      },
      error: (err) => {
        console.error("Errore durante il salvataggio:", err);
        this.messaggioErrore = "Impossibile salvare il video. Verifica che il tuo token sia valido e di avere i permessi STORE.";
        this.inviando = false;
        this.cdr.detectChanges(); // Mostriamo il banner rosso
      }
    });
  }

  private showFeedback(message: string, error: boolean): void {
    this.feedbackMessage = message;
    this.isError = error;
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
