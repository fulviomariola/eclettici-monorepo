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
import { PurchaseService } from '../../services/purchase.service';
import {QuizService, QuizDto, QuizResultDto} from '../../services/quiz.service';

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

  quizData: QuizDto | null = null;
  quizAnswers: { [questionId: number]: number } = {};
  quizResult: QuizResultDto | null = null;
  isQuizOpen: boolean = false;
  isSubmittingQuiz: boolean = false;

  caricaQuiz(courseId: number): void {
    console.log('🚀 Avvio richiesta Quiz per corso ID:', courseId);
    this.quizService.getQuizByCourse(courseId).subscribe({
      next: (quiz) => {
        console.log('✅ Risposta Quiz dal Server:', quiz);
        this.quizData = quiz;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Errore HTTP chiamata Quiz:', err);
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
    console.log('🔘 Risposta selezionata:', { questionId, optionId, statoAttuale: this.quizAnswers });
    this.cdr.detectChanges();
  }

  inviaQuiz(): void {
    console.log('🚀 Tentativo invio quiz...');
    console.log('Stato quizData:', this.quizData);
    console.log('Risposte registrate:', this.quizAnswers);

    if (!this.quizData || this.isSubmittingQuiz) {
      console.warn('⚠️ Invio bloccato: quizData nullo o invio già in corso');
      return;
    }

    const answersPayload = Object.keys(this.quizAnswers).map(qId => ({
      questionId: Number(qId),
      selectedOptionId: this.quizAnswers[Number(qId)]
    }));

    console.log('📦 Payload inviato al backend:', answersPayload);

    this.isSubmittingQuiz = true;
    this.quizService.submitQuiz(this.quizData.id, answersPayload).subscribe({
      next: (result) => {
        console.log('✅ Risultato Quiz ricevuto con successo:', result);
        this.quizResult = result;
        this.isSubmittingQuiz = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Errore HTTP durante la valutazione del quiz:', err);
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
      error: (err) => {
        console.error('Errore durante l\'eliminazione del commento:', err);
      }
    });
  }

  ngOnInit(): void {
    const token = localStorage.getItem('token') || localStorage.getItem('jwt');

    this.currentUserId = localStorage.getItem('user_id') || '';
    this.isLoggedIn = !!token;
    this.userRole = localStorage.getItem('user_role');

    this.route.paramMap.subscribe(params => {
      const courseIdParam = params.get('courseId');
      console.log('📌 Parametro ID Corso rilevato:', courseIdParam);
      if (courseIdParam) {
        this.currentCourseId = Number(courseIdParam);
        this.verificaAcquistoECaricaVideo(this.currentCourseId);
        this.caricaQuiz(this.currentCourseId); // <-- Questa riga avvia il recupero del Quiz
      }
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
      error: (err) => {
        console.error("Errore nel caricamento delle videolezioni del corso:", err);
      }
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

    // Sblocco consentito se l'utente è STORE/ADMIN oppure ha acquistato il singolo corso
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
      error: (err) => {
        console.error("Errore durante il salvataggio del commento:", err);
      }
    });
  }
}
