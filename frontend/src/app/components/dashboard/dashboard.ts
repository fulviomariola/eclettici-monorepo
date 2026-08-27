import { Component, OnInit, ViewChild, inject, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { EmailPanelComponent } from './email-panel/email-panel.component';
import { ProductsPanelComponent } from './products-panel/products-panel.component';

// IMPORT SOTTO-COMPONENTI
import { StorePanelComponent } from './store-panel/store-panel.component';
import { AdminPanelComponent } from './admin-panel/admin-panel.component';
import { CommunityBoardComponent } from './community-board/community-board.component';
import { ServicesPanelComponent } from './services-panel/services-panel.component';
import { ContactsPanelComponent } from './contacts-panel/contacts-panel.component';

import { PostService, PostResponseDto } from '../../services/post';
import { VideoService } from '../../services/video.service';
import { VideoDto } from '../../models/video';
import { CourseService } from '../../services/course.service';
import { ProgressService } from '../../services/progress.service';
import { CourseSummaryDto } from '../../models/course';
import { QuizService, UserCertificateDto } from '../../services/quiz.service';

export type DashboardTab = 'community' | 'management' | 'store' | 'email';

export interface CorsoAvanzamento {
  corso: CourseSummaryDto;
  completati: number;
  percentuale: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    StorePanelComponent,
    AdminPanelComponent,
    CommunityBoardComponent,
    ServicesPanelComponent,
    ContactsPanelComponent,
    EmailPanelComponent,
    ProductsPanelComponent
  ],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private router = inject(Router);
  private postService = inject(PostService);
  private videoService = inject(VideoService);
  private courseService = inject(CourseService);
  private progressService = inject(ProgressService);
  private quizService = inject(QuizService);
  private cdr = inject(ChangeDetectorRef);

  @ViewChild(CommunityBoardComponent) communityBoard!: CommunityBoardComponent;

  userEmail: string | null = '';
  currentUserId: string = '';
  userRole: string = '';

  activeTab: DashboardTab = 'community';

  postsList: PostResponseDto[] = [];
  videosList: VideoDto[] = [];
  showOnlyPrivate: boolean = false;

  // Dati e Statistiche Corsi Studente
  corsiIniziati: CorsoAvanzamento[] = [];
  totaleLezioniCompletate: number = 0;
  corsiCompletatiCount: number = 0;
  isLoadingCorsi: boolean = true;

  // Dati Certificati Conseguiti
  listaCertificati: UserCertificateDto[] = [];
  isLoadingCertificati: boolean = true;

  ngOnInit(): void {
    this.userEmail = localStorage.getItem('user_email');
    this.userRole = localStorage.getItem('user_role') || '';
    this.currentUserId = localStorage.getItem('user_id') || '';

    if (!this.userEmail || !this.currentUserId) {
      void this.router.navigate(['/login']);
      return;
    }

    if (this.userRole === 'ADMIN') {
      this.loadAdminStats();
    }

    this.caricaCorsiEProgressi();
    this.caricaCertificati();
  }

  caricaCorsiEProgressi(): void {
    this.isLoadingCorsi = true;
    this.courseService.getCourses().subscribe({
      next: (corsi) => {
        if (!corsi || corsi.length === 0) {
          this.isLoadingCorsi = false;
          this.cdr.detectChanges();
          return;
        }

        this.corsiIniziati = [];
        this.totaleLezioniCompletate = 0;
        this.corsiCompletatiCount = 0;
        let verificati = 0;

        corsi.forEach((corso) => {
          this.progressService.getCompletedLessons(corso.id).subscribe({
            next: (completedIds: string[]) => {
              const numCompletati = completedIds ? completedIds.length : 0;
              this.totaleLezioniCompletate += numCompletati;

              const percentuale = (corso.totalLessons && corso.totalLessons > 0)
                ? Math.min(100, Math.round((numCompletati / corso.totalLessons) * 100))
                : 0;

              if (percentuale === 100 && corso.totalLessons > 0) {
                this.corsiCompletatiCount++;
              }

              if (numCompletati > 0) {
                this.corsiIniziati.push({
                  corso,
                  completati: numCompletati,
                  percentuale
                });
              }

              verificati++;
              if (verificati === corsi.length) {
                this.isLoadingCorsi = false;
                this.cdr.detectChanges();
              }
            },
            error: () => {
              verificati++;
              if (verificati === corsi.length) {
                this.isLoadingCorsi = false;
                this.cdr.detectChanges();
              }
            }
          });
        });
      },
      error: (err) => {
        console.error('Errore nel caricamento corsi dashboard:', err);
        this.isLoadingCorsi = false;
        this.cdr.detectChanges();
      }
    });
  }

  caricaCertificati(): void {
    this.isLoadingCertificati = true;
    this.quizService.getUserCertificates().subscribe({
      next: (data) => {
        this.listaCertificati = data || [];
        this.isLoadingCertificati = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei certificati:', err);
        this.isLoadingCertificati = false;
        this.cdr.detectChanges();
      }
    });
  }

  scaricaCertificato(courseId: number): void {
    this.quizService.scaricaCertificato(courseId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Certificato_Corso_${courseId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Errore durante il download del certificato:', err)
    });
  }

  setTab(tab: DashboardTab): void {
    this.activeTab = tab;
  }

  loadAdminStats(): void {
    this.postService.getAllPosts(this.currentUserId, this.userRole).subscribe({
      next: (posts) => {
        this.postsList = posts;
        this.cdr.detectChanges();
      }
    });

    this.videoService.getVideosPremium().subscribe({
      next: (videos) => {
        this.videosList = videos;
        this.cdr.detectChanges();
      }
    });
  }

  onPostCreated(): void {
    if (this.communityBoard) {
      this.communityBoard.loadPosts();
    }
    if (this.userRole === 'ADMIN') {
      this.loadAdminStats();
    }
  }

  onVideoCreated(): void {
    if (this.userRole === 'ADMIN') {
      this.loadAdminStats();
    }
  }

  onFilterToggled(filterValue: boolean): void {
    this.showOnlyPrivate = filterValue;
  }

  onLogout(): void {
    localStorage.clear();
    void this.router.navigate(['/login']);
  }
}
