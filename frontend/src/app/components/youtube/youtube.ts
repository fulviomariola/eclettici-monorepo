import {ChangeDetectorRef, Component, inject, OnDestroy, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { VideoService } from '../../services/video.service'; // Sali di due livelli ed entra in services
import { AuthService } from '../../services/auth';
import { VideoDto } from '../../models/video';       // Sali di due livelli ed entra in models
import {Subscription } from 'rxjs';

@Component({
  selector: 'app-youtube',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './youtube.html'
})
export class YoutubeComponent implements OnInit {
  private videoService = inject(VideoService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  listaVideo: VideoDto[] = [];
  messaggioErrore: string | null = null;
  ruoloUtente: string | null = null;

  // Sottoscrizione per evitare memory leak
  private subRuolo: Subscription | null = null;

  ngOnInit(): void {
    // Ascoltiamo il ruolo dell'utente in tempo reale
    this.subRuolo = this.authService.userRole$.subscribe({
      next: (ruolo) => {
        this.ruoloUtente = ruolo;
        this.selezionaEcaricaCatalogo();
      }
    });
  }

  private selezionaEcaricaCatalogo(): void {
    // Se l'utente è STUDENT o STORE, carichiamo tutto il catalogo (inclusi i premium)
    if (this.ruoloUtente === 'STUDENT' || this.ruoloUtente === 'STORE') {
      this.caricaContenutiPremium();
    } else {
      // Se l'utente è anonimo, carichiamo solo quelli pubblici
      this.caricaContenutiPubblici();
    }
  }

  private caricaContenutiPubblici(): void {
    this.videoService.getVideosPubblici().subscribe({
      next: (dati) => {
        this.listaVideo = dati;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei video:', err);
       // this.messaggioErrore = 'Impossibile caricare il catalogo dei video. Riprova più tardi.';
        this.gestisciErrore(err);
        //this.cdr.detectChanges();
      }
    });
  }

  private caricaContenutiPremium(): void {
    this.videoService.getVideosPremium().subscribe({
      next: (dati) => {
        this.listaVideo = dati;
        this.cdr.detectChanges();
      }
    });
  }

  private gestisciErrore(err: any): void {
    console.error('Errore nel caricamento del catalogo video:', err);
    this.messaggioErrore = 'Impossibile caricare il catalogo dei video. Riprova più tardi.';
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    // Ci disiscriviamo quando distruggiamo il componente
    if (this.subRuolo) {
      this.subRuolo.unsubscribe();
    }
  }
}

















