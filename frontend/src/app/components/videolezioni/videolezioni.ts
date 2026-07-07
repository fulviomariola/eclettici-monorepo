import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // 1. Fondamentale per far funzionare [(ngModel)] sulla textarea
import { VideoService } from '../../services/video.service';
import { VideoDto } from '../../models/video';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';
import { ProgressService } from '../../services/progress.service';
import { CommentService } from '../../services/comment'; // 2. Import del nuovo servizio commenti
import { CommentResponseDto } from '../../models/comment'; // Import del DTO di risposta

@Component({
  selector: 'app-videolezioni',
  templateUrl: './videolezioni.html',
  standalone: true,
  imports: [CommonModule, SafeUrlPipe, FormsModule] // 3. Aggiunto FormsModule nell'architettura Standalone
})
export class VideolezioniComponent implements OnInit {
  private videoService = inject(VideoService);
  private progressService = inject(ProgressService);
  private commentService = inject(CommentService); // 4. Iniezione del servizio commenti
  private cdr = inject(ChangeDetectorRef);

  listaVideo: VideoDto[] = [];
  videoSelezionato: VideoDto | null = null;
  mostraBannerUpgrade: boolean = false;
  userRole: string | null = null;

  videoIsCompleted: boolean = false;

  // ================= VARIABILI DI STATO FASE C =================
  listaCommenti: CommentResponseDto[] = []; // Ospediterà i commenti del video attivo
  nuovoCommentoTesto: string = ''; // Catturerà il testo scritto dall'utente nella textarea

  ngOnInit(): void {
    this.userRole = localStorage.getItem('user_role');
    this.caricaVideolezioni();
  }

  caricaVideolezioni(): void {
    const token = localStorage.getItem('token');
    const dataObservable = token
      ? this.videoService.getVideosPremium()
      : this.videoService.getVideosPubblici();

    dataObservable.subscribe({
      next: (videos) => {
        this.listaVideo = videos;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore nel caricamento delle videolezioni:", err);
      }
    });
  }

  selezionaVideo(video: VideoDto): void {
    this.userRole = localStorage.getItem('user_role');

    console.log('--- DEBUG SELEZIONA VIDEO ---');
    console.log('Ruolo Utente attuale:::: ', this.userRole);
    console.log('Il video è premium?:::: ', video.premium);

    if (video.premium && this.userRole !== 'STORE') {
      this.mostraBannerUpgrade = true;
      this.videoSelezionato = null;
    } else {
      this.mostraBannerUpgrade = false;
      this.videoSelezionato = video;

      // 5. Reset preventivo dell'array per non mostrare i commenti del video precedente
      this.listaCommenti = [];

      if (video.videoId) {
        // ================= LOGICA DI RECUPERO STATO PROGRESSO (FASE B) =================
        this.progressService.getProgressoVideo(video.videoId).subscribe({
          next: (response) => {
            this.videoIsCompleted = response.isCompleted;
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error("Errore nel recupero dello stato di avanzamento:", err);
            this.videoIsCompleted = false;
            this.cdr.detectChanges();
          }
        });

        // ================= NUOVA LOGICA DI RECUPERO COMMENTI (FASE C) =================
        this.commentService.getCommentiPerVideo(video.videoId).subscribe({
          next: (comments) => {
            this.listaCommenti = comments; // Carichiamo i commenti arrivati dal database
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error("Errore nel recupero dei commenti del video:", err);
            this.listaCommenti = [];
            this.cdr.detectChanges();
          }
        });
      }
    }
    this.cdr.detectChanges();
  }

  toggleCompleto(event: any): void {
    if (!this.videoSelezionato || !this.videoSelezionato.videoId) return;

    const completato = event.target.checked;

    this.progressService.aggiornaProgresso(this.videoSelezionato.videoId, completato).subscribe({
      next: (response) => {
        this.videoIsCompleted = response.isCompleted;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.log("Errore durante il salvataggio del progresso:", err);
        event.target.checked = !completato;
      }
    });
  }

  // ================= NUOVO METODO PER PUBBLICARE UN COMMENTO (FASE C) =================
  /**
   * Prende il testo dalla textarea e invia il DTO al backend.
   */
  aggiungiCommento(): void {
    // Validazione di sicurezza: evita l'invio se il testo è vuoto o manca il video selezionato
    if (!this.nuovoCommentoTesto.trim() || !this.videoSelezionato || !this.videoSelezionato.videoId) {
      return;
    }

    const payload = {
      content: this.nuovoCommentoTesto,
      videoId: this.videoSelezionato.videoId
    };

    this.commentService.createComment(payload).subscribe({
      next: (commentoSalvato) => {
        // Ottimizzazione UX: iniettiamo il commento in cima alla lista locale istantaneamente
        this.listaCommenti.unshift(commentoSalvato);

        // Resettiamo il campo di testo della textarea per pulire l'interfaccia
        this.nuovoCommentoTesto = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio del commento:", err);
      }
    });
  }
}
