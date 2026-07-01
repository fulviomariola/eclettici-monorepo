import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VideoService } from '../../services/video.service';
import { VideoDto } from '../../models/video';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';

@Component({
  selector: 'app-videolezioni',
  templateUrl: './videolezioni.html',
  standalone: true,
  imports: [CommonModule, SafeUrlPipe]
})
export class VideolezioniComponent implements OnInit {
  private videoService = inject(VideoService);
  private cdr = inject(ChangeDetectorRef);

  listaVideo: VideoDto[] = [];
  videoSelezionato: VideoDto | null = null;
  mostraBannerUpgrade: boolean = false;
  userRole: string | null = null;

  ngOnInit(): void {
    // Recuperiamo il ruolo direttamente dal localStorage allineato al tuo authGuard
    this.userRole = localStorage.getItem('user_role');
    this.caricaVideolezioni();
  }

  caricaVideolezioni(): void {
    const token = localStorage.getItem('token');

    // Se l'utente è loggato carichiamo tutto il catalogo (pubblici + premium)
    // Se è anonimo carichiamo solo i pubblici
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
    // Aggiorna il ruolo per sicurezza in caso di modifiche runtime
    this.userRole = localStorage.getItem('user_role');

    console.log('--- DEBUG SELEZIONA VIDEO ---');
    console.log('Ruolo Utente attuale:::: ', this.userRole);
    console.log('Il video è premium?:::: ', video.premium);
    // console.log('Tipo di dato video.premium::: ', typeof video.premium);


    if (video.premium && this.userRole !== 'STORE') {
      // L'utente è STUDENT o anonimo -> Blocca il video e mostra il banner
      this.mostraBannerUpgrade = true;
      this.videoSelezionato = null;
    } else {
      // L'utente è STORE o il video è gratuito -> Sblocca il player
      this.mostraBannerUpgrade = false;
      this.videoSelezionato = video;
    }
    // 3. Forziamo Angular a ridisegnare subito l'interfaccia HTML
    this.cdr.detectChanges();
  }
}
