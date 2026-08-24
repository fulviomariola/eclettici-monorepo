import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { CourseService } from '../../services/course.service';
import { VideoDto } from '../../models/video';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';
import { ProgressService } from '../../services/progress.service';
import { CommentService } from '../../services/comment';
import { CommentResponseDto } from '../../models/comment';

export type StatoVisione = 'RIPRODUCI' | 'BLOCCO_LOGIN_GRATIS' | 'BLOCCO_PREMIUM';

@Component({
  selector: 'app-videolezioni',
  templateUrl: './videolezioni.html',
  standalone: true,
  imports: [CommonModule, SafeUrlPipe, FormsModule, RouterLink]
})
export class VideolezioniComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private courseService = inject(CourseService);
  private progressService = inject(ProgressService);
  private commentService = inject(CommentService);
  private cdr = inject(ChangeDetectorRef);

  currentCourseId: number | null = null;
  listaVideo: VideoDto[] = [];
  videoSelezionato: VideoDto | null = null;
  statoVisione: StatoVisione = 'RIPRODUCI';

  userRole: string | null = null;
  isLoggedIn: boolean = false;
  videoIsCompleted: boolean = false;

  listaCommenti: CommentResponseDto[] = [];
  nuovoCommentoTesto: string = '';

  ngOnInit(): void {
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');
    this.isLoggedIn = !!token;
    this.userRole = localStorage.getItem('user_role');

    // Legge l'ID del corso dalla rotta /videolezioni/:courseId
    this.route.paramMap.subscribe(params => {
      const courseIdParam = params.get('courseId');
      if (courseIdParam) {
        this.currentCourseId = Number(courseIdParam);
        this.caricaVideolezioniCorso(this.currentCourseId);
      }
    });
  }

  caricaVideolezioniCorso(courseId: number): void {
    this.courseService.getVideosByCourse(courseId).subscribe({
      next: (videos) => {
        this.listaVideo = videos;
        if (this.listaVideo.length > 0) {
          this.selezionaVideo(this.listaVideo[0]);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore nel caricamento delle videolezioni del corso:", err);
      }
    });
  }

  selezionaVideo(video: VideoDto): void {
    this.userRole = localStorage.getItem('user_role');
    this.videoSelezionato = video;
    this.listaCommenti = [];
    this.videoIsCompleted = false;

    const indice = this.listaVideo.indexOf(video);
    const isAnteprimaLibera = (indice === 0) && !video.premium;
    const idEffettivo = video.videoId || (video as any).id;

    if (video.premium) {
      this.statoVisione = (this.userRole === 'STORE') ? 'RIPRODUCI' : 'BLOCCO_PREMIUM';
    } else {
      this.statoVisione = (this.isLoggedIn || isAnteprimaLibera) ? 'RIPRODUCI' : 'BLOCCO_LOGIN_GRATIS';
    }

    if (this.statoVisione === 'RIPRODUCI' && idEffettivo) {
      if (this.isLoggedIn) {
        this.progressService.getProgressoVideo(idEffettivo).subscribe({
          next: (res) => {
            this.videoIsCompleted = res.isCompleted;
            this.cdr.detectChanges();
          },
          error: () => {
            this.videoIsCompleted = false;
            this.cdr.detectChanges();
          }
        });
      }

      this.commentService.getCommentiPerVideo(idEffettivo).subscribe({
        next: (comments) => {
          this.listaCommenti = comments;
          this.cdr.detectChanges();
        },
        error: () => {
          this.listaCommenti = [];
          this.cdr.detectChanges();
        }
      });
    }

    this.cdr.detectChanges();
  }

  toggleCompleto(event: any): void {
    if (!this.isLoggedIn || !this.videoSelezionato?.videoId) return;

    const completato = event.target.checked;
    this.progressService.aggiornaProgresso(this.videoSelezionato.videoId, completato).subscribe({
      next: (res) => {
        this.videoIsCompleted = res.isCompleted;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio del progresso:", err);
        event.target.checked = !completato;
      }
    });
  }

  aggiungiCommento(): void {
    if (!this.isLoggedIn || !this.nuovoCommentoTesto.trim() || !this.videoSelezionato?.videoId) {
      return;
    }

    const payload = {
      content: this.nuovoCommentoTesto,
      videoId: this.videoSelezionato.videoId
    };

    this.commentService.createComment(payload).subscribe({
      next: (nuovoCommento) => {
        this.listaCommenti.unshift(nuovoCommento);
        this.nuovoCommentoTesto = '';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio del commento:", err);
      }
    });
  }
}
