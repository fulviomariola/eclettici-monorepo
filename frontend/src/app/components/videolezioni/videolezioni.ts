import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { CourseService, CourseRatingSummaryDto, CourseReviewDto } from '../../services/course.service';
import { VideoDto } from '../../models/video';
import { SafeUrlPipe } from '../../pipes/safe-url.pipe';
import { ProgressService } from '../../services/progress.service';
import { CommentService } from '../../services/comment';
import { CommentResponseDto } from '../../models/comment';
import { PurchaseService } from '../../services/purchase.service';
import { QuizService, QuizDto, QuizResultDto } from '../../services/quiz.service';
import { ProjectService, ProjectSubmissionDto } from '../../services/project.service';

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
  private purchaseService = inject(PurchaseService);
  private cdr = inject(ChangeDetectorRef);
  private quizService = inject(QuizService);
  private projectService = inject(ProjectService);

  currentCourseId: number | null = null;
  listaVideo: VideoDto[] = [];
  videoSelezionato: VideoDto | null = null;
  statoVisione: StatoVisione = 'RIPRODUCI';
  userRole: string | null = null;
  isLoggedIn: boolean = false;
  isCoursePurchased: boolean = false;
  isPurchasing: boolean = false;

  completedVideoIds: Set<number> = new Set<number>();
  listaCommenti: CommentResponseDto[] = [];
  nuovoCommentoTesto: string = '';
  currentPage: number = 1;
  pageSize: number = 25;
  mostraPlayer: boolean = true;
  currentUserId: string = '';

  // Gestione Quiz
  quizData: QuizDto | null = null;
  quizAnswers: { [questionId: number]: number } = {};
  quizResult: QuizResultDto | null = null;
  isQuizOpen: boolean = false;
  isSubmittingQuiz: boolean = false;

  // Gestione Recensioni
  ratingSummary: CourseRatingSummaryDto = { averageRating: 0, totalReviews: 0, reviews: [] };
  selectedRating: number = 5;
  reviewComment: string = '';
  isSubmittingReview: boolean = false;
  reviewSuccessMessage: string | null = null;

  // Gestione Consegna Progetto Pratico
  mySubmission: ProjectSubmissionDto | null = null;
  repoUrlInput: string = '';
  projectNotesInput: string = '';
  isSubmittingProject: boolean = false;
  projectMessage: string | null = null;

  ngOnInit(): void {
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');

    this.currentUserId = localStorage.getItem('user_id') || '';
    this.isLoggedIn = !!token;
    this.userRole = localStorage.getItem('user_role');

    this.route.paramMap.subscribe(params => {
      const courseIdParam = params.get('courseId');
      if (courseIdParam) {
        this.currentCourseId = Number(courseIdParam);
        this.verificaAcquistoECaricaVideo(this.currentCourseId);
        this.caricaQuiz(this.currentCourseId);
        this.caricaRecensioni(this.currentCourseId);
        if (this.isLoggedIn) {
          this.caricaProgettoInviato(this.currentCourseId);
        }
      }
    });
  }

  caricaProgettoInviato(courseId: number): void {
    this.projectService.getMySubmission(courseId).subscribe({
      next: (sub) => {
        this.mySubmission = sub;
        if (sub) {
          this.repoUrlInput = sub.repoUrl;
          this.projectNotesInput = sub.notes;
        }
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  inviaProgetto(): void {
    if (!this.currentCourseId || !this.repoUrlInput.trim() || this.isSubmittingProject) return;

    this.isSubmittingProject = true;
    this.projectMessage = null;

    this.projectService.submitProject(this.currentCourseId, this.repoUrlInput, this.projectNotesInput).subscribe({
      next: (saved) => {
        this.mySubmission = saved;
        this.isSubmittingProject = false;
        this.projectMessage = 'Progetto inviato con successo! È in attesa di revisione da parte del docente.';
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore durante l\'invio del progetto:', err);
        this.isSubmittingProject = false;
        this.projectMessage = 'Errore durante l\'invio del progetto.';
        this.cdr.detectChanges();
      }
    });
  }

  caricaRecensioni(courseId: number): void {
    this.courseService.getCourseReviews(courseId).subscribe({
      next: (summary) => {
        this.ratingSummary = summary;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  setRating(stars: number): void {
    this.selectedRating = stars;
  }

  inviaRecensione(): void {
    if (!this.currentCourseId || !this.isLoggedIn || this.isSubmittingReview) return;

    this.isSubmittingReview = true;
    this.reviewSuccessMessage = null;

    this.courseService.submitCourseReview(this.currentCourseId, this.selectedRating, this.reviewComment).subscribe({
      next: () => {
        this.isSubmittingReview = false;
        this.reviewSuccessMessage = 'Grazie per la tua recensione!';
        this.reviewComment = '';
        this.caricaRecensioni(this.currentCourseId!);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore invio recensione:', err);
        this.isSubmittingReview = false;
        this.cdr.detectChanges();
      }
    });
  }

  caricaQuiz(courseId: number): void {
    this.quizService.getQuizByCourse(courseId).subscribe({
      next: (quiz) => {
        this.quizData = quiz;
        this.cdr.detectChanges();
      },
      error: () => {
        this.quizData = null;
        this.cdr.detectChanges();
      }
    });
  }

  scaricaCertificato(): void {
    if (!this.currentCourseId) return;

    this.quizService.scaricaCertificato(this.currentCourseId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Certificato_Corso_${this.currentCourseId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Errore durante il download del certificato:', err)
    });
  }

  apriQuiz(): void {
    this.isQuizOpen = true;
    this.quizResult = null;
    this.quizAnswers = {};
  }

  chiudiQuiz(): void {
    this.isQuizOpen = false;
  }

  selezionaRisposta(questionId: number, optionId: number): void {
    this.quizAnswers[questionId] = optionId;
    this.cdr.detectChanges();
  }

  inviaQuiz(): void {
    if (!this.quizData || this.isSubmittingQuiz) return;

    const answersPayload = Object.keys(this.quizAnswers).map(qId => ({
      questionId: Number(qId),
      selectedOptionId: this.quizAnswers[Number(qId)]
    }));

    this.isSubmittingQuiz = true;
    this.quizService.submitQuiz(this.quizData.id, answersPayload).subscribe({
      next: (result) => {
        this.quizResult = result;
        this.isSubmittingQuiz = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSubmittingQuiz = false;
        this.cdr.detectChanges();
      }
    });
  }

  togglePlayer(): void {
    this.mostraPlayer = !this.mostraPlayer;
  }

  get currentIndex(): number {
    if (!this.videoSelezionato || !this.listaVideo) return -1;
    return this.listaVideo.findIndex(v => v.youtubeId === this.videoSelezionato?.youtubeId);
  }

  get hasPrevious(): boolean {
    return this.currentIndex > 0;
  }

  get hasNext(): boolean {
    const idx = this.currentIndex;
    return idx >= 0 && idx < this.listaVideo.length - 1;
  }

  lezionePrecedente(): void {
    const prevIdx = this.currentIndex - 1;
    if (prevIdx >= 0) {
      const prevVideo = this.listaVideo[prevIdx];
      this.selezionaVideo(prevVideo);
      this.sincronizzaPagina(prevIdx);
    }
  }

  lezioneSuccessiva(segnaCompletata: boolean = true): void {
    const currentIdx = this.currentIndex;
    const nextIdx = currentIdx + 1;

    if (nextIdx < this.listaVideo.length) {
      if (segnaCompletata && this.isLoggedIn && !this.currentVideoIsCompleted) {
        this.toggleCompleto({ target: { checked: true } });
      }

      const nextVideo = this.listaVideo[nextIdx];
      this.selezionaVideo(nextVideo);
      this.sincronizzaPagina(nextIdx);
    }
  }

  private sincronizzaPagina(videoIndex: number): void {
    const paginaDestinazione = Math.floor(videoIndex / this.pageSize) + 1;
    if (this.currentPage !== paginaDestinazione) {
      this.currentPage = paginaDestinazione;
    }
  }

  get paginatedVideos(): any[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.listaVideo.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.listaVideo.length / this.pageSize) || 1;
  }

  cambiaPagina(nuovaPagina: number): void {
    if (nuovaPagina >= 1 && nuovaPagina <= this.totalPages) {
      this.currentPage = nuovaPagina;
      if (window.innerWidth < 1024) {
        document.getElementById('player-container')?.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }

  get completedLessonsCount(): number {
    return this.listaVideo.filter(v => this.isCompleted(v)).length;
  }

  get progressPercentage(): number {
    if (!this.listaVideo || this.listaVideo.length === 0) return 0;
    return Math.round((this.completedLessonsCount / this.listaVideo.length) * 100);
  }

  eliminaCommento(commentId: any): void {
    if (!confirm('Sei sicuro di voler eliminare questo commento?')) return;

    this.commentService.deleteComment(commentId).subscribe({
      next: () => {
        this.listaCommenti = this.listaCommenti.filter(c => c.id !== commentId);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Errore durante l\'eliminazione del commento:', err)
    });
  }

  verificaAcquistoECaricaVideo(courseId: number): void {
    if (this.isLoggedIn) {
      this.purchaseService.checkPurchase(courseId).subscribe({
        next: (res) => {
          this.isCoursePurchased = res.purchased;
          this.caricaVideolezioniCorso(courseId);
        },
        error: () => {
          this.isCoursePurchased = false;
          this.caricaVideolezioniCorso(courseId);
        }
      });
    } else {
      this.isCoursePurchased = false;
      this.caricaVideolezioniCorso(courseId);
    }
  }

  caricaVideolezioniCorso(courseId: number): void {
    this.courseService.getVideosByCourse(courseId).subscribe({
      next: (videos) => {
        this.listaVideo = videos;

        if (this.listaVideo.length > 0) {
          if (this.isLoggedIn) {
            this.avviaDalPuntoInterrotto(courseId);
          } else {
            this.selezionaVideo(this.listaVideo[0]);
          }
        }

        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore nel caricamento delle videolezioni:", err)
    });
  }

  avviaDalPuntoInterrotto(courseId: number): void {
    this.progressService.getCompletedLessons(courseId).subscribe({
      next: (completedYoutubeIds: string[]) => {
        const completatiSet = new Set(completedYoutubeIds || []);
        const indexDaAvviare = this.listaVideo.findIndex(
          video => !completatiSet.has(video.youtubeId)
        );

        const targetIndex = indexDaAvviare !== -1 ? indexDaAvviare : 0;
        const targetVideo = this.listaVideo[targetIndex];

        this.selezionaVideo(targetVideo);
        this.sincronizzaPagina(targetIndex);
        this.caricaStatoCompletamenti();
      },
      error: () => {
        this.selezionaVideo(this.listaVideo[0]);
        this.caricaStatoCompletamenti();
      }
    });
  }

  caricaStatoCompletamenti(): void {
    this.listaVideo.forEach(video => {
      const idEffettivo = video.videoId || (video as any).id;
      if (idEffettivo) {
        this.progressService.getProgressoVideo(idEffettivo).subscribe({
          next: (res) => {
            if (res.isCompleted) {
              this.completedVideoIds.add(idEffettivo);
            } else {
              this.completedVideoIds.delete(idEffettivo);
            }
            this.cdr.detectChanges();
          },
          error: () => {}
        });
      }
    });
  }

  isCompleted(video: VideoDto | null): boolean {
    if (!video) return false;
    const id = video.videoId || (video as any).id;
    return this.completedVideoIds.has(id);
  }

  get currentVideoIsCompleted(): boolean {
    return this.isCompleted(this.videoSelezionato);
  }

  selezionaVideo(video: VideoDto): void {
    this.userRole = localStorage.getItem('user_role');
    this.videoSelezionato = video;
    this.listaCommenti = [];
    this.mostraPlayer = true;

    const indice = this.listaVideo.indexOf(video);
    const isAnteprimaLibera = (indice === 0) && !video.premium;
    const idEffettivo = video.videoId || (video as any).id;

    if (video.premium) {
      const haAccesso = (this.userRole === 'STORE' || this.userRole === 'ADMIN' || this.isCoursePurchased);
      this.statoVisione = haAccesso ? 'RIPRODUCI' : 'BLOCCO_PREMIUM';
    } else {
      this.statoVisione = (this.isLoggedIn || isAnteprimaLibera) ? 'RIPRODUCI' : 'BLOCCO_LOGIN_GRATIS';
    }

    if (this.statoVisione === 'RIPRODUCI' && idEffettivo) {
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

  acquistaCorso(): void {
    if (!this.currentCourseId || !this.isLoggedIn || this.isPurchasing) return;

    this.isPurchasing = true;
    this.purchaseService.buyCourse(this.currentCourseId).subscribe({
      next: () => {
        this.isPurchasing = false;
        this.isCoursePurchased = true;
        if (this.videoSelezionato) {
          this.selezionaVideo(this.videoSelezionato);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante l'acquisto del corso:", err);
        this.isPurchasing = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleCompleto(event: any): void {
    const idEffettivo = this.videoSelezionato?.videoId || (this.videoSelezionato as any)?.id;
    if (!this.isLoggedIn || !idEffettivo) return;

    const completato = event.target.checked;
    this.progressService.aggiornaProgresso(idEffettivo, completato).subscribe({
      next: (res) => {
        if (res.isCompleted) {
          this.completedVideoIds.add(idEffettivo);
        } else {
          this.completedVideoIds.delete(idEffettivo);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Errore durante il salvataggio del progresso:", err);
        event.target.checked = !completato;
      }
    });
  }

  aggiungiCommento(): void {
    const idEffettivo = this.videoSelezionato?.videoId || (this.videoSelezionato as any)?.id;
    if (!this.isLoggedIn || !this.nuovoCommentoTesto.trim() || !idEffettivo) {
      return;
    }

    const payload = {
      content: this.nuovoCommentoTesto,
      videoId: idEffettivo
    };

    this.commentService.createComment(payload).subscribe({
      next: (nuovoCommento) => {
        this.listaCommenti.unshift(nuovoCommento);
        this.nuovoCommentoTesto = '';
        this.cdr.detectChanges();
      },
      error: (err) => console.error("Errore durante il salvataggio del commento:", err)
    });
  }
}
