import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CourseService, CourseRatingSummaryDto } from '../../services/course.service';
import { AuthService } from '../../services/auth';
import { ProgressService } from '../../services/progress.service';
import { CourseSummaryDto } from '../../models/course';

@Component({
  selector: 'app-catalogo-corsi',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './catalogo-corsi.html'
})
export class CatalogoCorsiComponent implements OnInit {
  private courseService = inject(CourseService);
  private authService = inject(AuthService);
  private progressService = inject(ProgressService);
  private cdr = inject(ChangeDetectorRef);

  listaCorsi: CourseSummaryDto[] = [];
  isLoading = true;
  isLoggedIn = false;
  completedLessonsMap: { [courseId: number]: number } = {};
  ratingsMap: { [courseId: number]: { average: number; total: number } } = {};

  testoRicerca: string = '';
  filtroSelezionato: 'TUTTI' | 'GRATIS' | 'PREMIUM' = 'TUTTI';

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.caricaCatalogo();
  }

  caricaCatalogo(): void {
    this.courseService.getCourses().subscribe({
      next: (corsi) => {
        this.listaCorsi = corsi;
        this.isLoading = false;

        this.caricaValutazioni();

        if (this.isLoggedIn) {
          this.caricaTuttiIProgressi();
        } else {
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Errore durante il caricamento dei corsi:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  caricaValutazioni(): void {
    this.listaCorsi.forEach(corso => {
      this.courseService.getCourseReviews(corso.id).subscribe({
        next: (res: CourseRatingSummaryDto) => {
          this.ratingsMap[corso.id] = {
            average: res.averageRating,
            total: res.totalReviews
          };
          this.cdr.detectChanges();
        },
        error: () => {
          this.ratingsMap[corso.id] = { average: 0, total: 0 };
        }
      });
    });
  }

  getRating(courseId: number): { average: number; total: number } {
    return this.ratingsMap[courseId] || { average: 0, total: 0 };
  }

  caricaTuttiIProgressi(): void {
    this.listaCorsi.forEach((corso) => {
      this.progressService.getCompletedLessons(corso.id).subscribe({
        next: (completedIds: string[]) => {
          this.completedLessonsMap[corso.id] = completedIds ? completedIds.length : 0;
          this.cdr.detectChanges();
        },
        error: () => {
          this.completedLessonsMap[corso.id] = 0;
        }
      });
    });
  }

  getCompletedCount(corso: CourseSummaryDto): number {
    return this.completedLessonsMap[corso.id] || 0;
  }

  getProgressPercentage(corso: CourseSummaryDto): number {
    if (!corso.totalLessons || corso.totalLessons === 0) return 0;
    const completed = this.getCompletedCount(corso);
    return Math.min(100, Math.round((completed / corso.totalLessons) * 100));
  }

  get corsiFiltrati(): CourseSummaryDto[] {
    const query = this.testoRicerca.toLowerCase().trim();

    return this.listaCorsi.filter(corso => {
      const matchTesto = !query ||
        corso.title.toLowerCase().includes(query) ||
        (corso.description && corso.description.toLowerCase().includes(query));

      const matchTipo =
        this.filtroSelezionato === 'TUTTI' ||
        (this.filtroSelezionato === 'PREMIUM' && corso.isPremium) ||
        (this.filtroSelezionato === 'GRATIS' && !corso.isPremium);

      return matchTesto && matchTipo;
    });
  }

  impostaFiltro(tipo: 'TUTTI' | 'GRATIS' | 'PREMIUM'): void {
    this.filtroSelezionato = tipo;
  }
}
