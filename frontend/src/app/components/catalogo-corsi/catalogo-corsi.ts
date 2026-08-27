import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms'; // <-- Aggiunto FormsModule
import { CourseService } from '../../services/course.service';
import { AuthService } from '../../services/auth';
import { ProgressService } from '../../services/progress.service';
import { CourseSummaryDto } from '../../models/course';

@Component({
  selector: 'app-catalogo-corsi',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule], // <-- Inserito negli imports
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

  // Parametri per i filtri di ricerca
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

  // Restituisce solo i corsi che soddisfano il testo cercato e il tipo (Gratis / Premium)
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
