import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CourseService } from '../../services/course.service';
import { CourseSummaryDto } from '../../models/course';

@Component({
  selector: 'app-catalogo-corsi',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './catalogo-corsi.html'
})
export class CatalogoCorsiComponent implements OnInit {
  private courseService = inject(CourseService);
  private cdr = inject(ChangeDetectorRef);

  listaCorsi: CourseSummaryDto[] = [];
  isLoading = true;

  ngOnInit(): void {
    this.caricaCatalogo();
  }

  caricaCatalogo(): void {
    this.courseService.getCourses().subscribe({
      next: (corsi) => {
        this.listaCorsi = corsi;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore durante il caricamento dei corsi:', err);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
