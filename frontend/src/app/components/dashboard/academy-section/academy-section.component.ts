import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VideoDto } from '../../../models/video';
import { VideoService } from '../../../services/video.service';
import { ProgressService } from '../../../services/progress.service';
import { SafeUrlPipe } from '../../../pipes/safe-url.pipe';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-academy-section',
  standalone: true,
  imports: [CommonModule, SafeUrlPipe, ScrollFadeDirective],
  templateUrl: './academy-section.component.html'
})
export class AcademySectionComponent implements OnInit {
  private videoService = inject(VideoService);
  private progressService = inject(ProgressService);
  private cdr = inject(ChangeDetectorRef);

  videosList: VideoDto[] = [];
  percentualeAvanzamento: number = 0;
  completedVideoIds: Set<number> = new Set<number>();

  ngOnInit(): void {
    this.loadVideos();
  }

  loadVideos(): void {
    this.videoService.getVideosPremium().subscribe({
      next: (data) => {
        this.videosList = data;
        this.loadUserProgress();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Errore caricamento video:', err)
    });
  }

  loadUserProgress(): void {
    this.progressService.getPercentualeAvanzamento().subscribe({
      next: (res) => {
        this.percentualeAvanzamento = res.percentuale;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Errore recupero percentuale:', err)
    });

    this.videosList.forEach(video => {
      if (video.videoId) {
        const vId = video.videoId;
        this.progressService.getProgressoVideo(vId).subscribe({
          next: (res: { isCompleted: boolean }) => {
            if (res.isCompleted) {
              this.completedVideoIds.add(vId);
            } else {
              this.completedVideoIds.delete(vId);
            }
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  toggleVideoProgress(videoId: number | undefined): void {
    if (!videoId) return;

    const nuovoStato = !this.completedVideoIds.has(videoId);

    this.progressService.aggiornaProgresso(videoId, nuovoStato).subscribe({
      next: () => {
        if (nuovoStato) {
          this.completedVideoIds.add(videoId);
        } else {
          this.completedVideoIds.delete(videoId);
        }

        this.progressService.getPercentualeAvanzamento().subscribe({
          next: (res) => {
            this.percentualeAvanzamento = res.percentuale;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => console.error("Errore durante l'aggiornamento del progresso:", err)
    });
  }

  isVideoCompleted(videoId: number | undefined): boolean {
    if (!videoId) return false;
    return this.completedVideoIds.has(videoId);
  }
}
