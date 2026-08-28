import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { QuizService, AdminQuizDto, AdminQuizQuestion, AdminQuizOption } from '../../services/quiz.service';
import { API_BASE_URL } from '../../config/api.config';

interface SimpleCourse {
  id: number;
  title: string;
}

@Component({
  selector: 'app-gestione-quiz',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-white p-4 sm:p-8">
      <div class="max-w-5xl mx-auto space-y-6">

        <!-- Intestazione -->
        <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-slate-900 border border-slate-800 p-6 rounded-2xl">
          <div>
            <h1 class="text-2xl font-bold text-white">Pannello Gestione Quiz</h1>
            <p class="text-xs text-slate-400 mt-1">Configura i test finali, le domande e le risposte esatte per ciascun corso</p>
          </div>
          <a routerLink="/gestione-video" class="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-semibold rounded-xl transition no-underline">
            ← Gestione Video
          </a>
        </div>

        <!-- Selezione Corso -->
        <div class="bg-slate-900 border border-slate-800 p-6 rounded-2xl space-y-4">
          <label class="block text-xs font-bold text-slate-300 uppercase tracking-wider">Seleziona Corso da Modificare:</label>
          <div class="flex flex-col sm:flex-row gap-3">
            <select
              [(ngModel)]="selectedCourseId"
              (change)="onCourseChange()"
              class="flex-1 bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-indigo-500">
              <option [ngValue]="null" disabled>-- Seleziona un corso --</option>
              <option *ngFor="let course of courses" [ngValue]="course.id">
                #{{ course.id }} - {{ course.title }}
              </option>
            </select>
          </div>
        </div>

        <!-- Messaggi di Notifica -->
        <div *ngIf="successMessage" class="p-4 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-medium rounded-xl">
          ✓ {{ successMessage }}
        </div>
        <div *ngIf="errorMessage" class="p-4 bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-medium rounded-xl">
          ⚠ {{ errorMessage }}
        </div>

        <!-- Form Quiz (Visualizzato se un corso è selezionato) -->
        <div *ngIf="selectedCourseId" class="bg-slate-900 border border-slate-800 p-6 rounded-2xl space-y-6">

          <!-- Parametri Base Quiz -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="md:col-span-2 space-y-1">
              <label class="text-xs font-bold text-slate-400">Titolo del Quiz</label>
              <input
                type="text"
                [(ngModel)]="currentQuiz.title"
                placeholder="Es. Test Finale di Verifica"
                class="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-indigo-500" />
            </div>

            <div class="space-y-1">
              <label class="text-xs font-bold text-slate-400">Punteggio Minimo di Superamento (%)</label>
              <input
                type="number"
                min="1"
                max="100"
                [(ngModel)]="currentQuiz.passingScore"
                class="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-indigo-500" />
            </div>
          </div>

          <hr class="border-slate-800" />

          <!-- Lista Domande -->
          <div class="space-y-6">
            <div class="flex justify-between items-center">
              <h2 class="text-base font-bold text-slate-200">Domande del Test ({{ currentQuiz.questions.length }})</h2>
              <button
                type="button"
                (click)="addQuestion()"
                class="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold rounded-xl transition shadow">
                + Aggiungi Domanda
              </button>
            </div>

            <div *ngIf="currentQuiz.questions.length === 0" class="text-center py-8 text-slate-500 text-xs border border-dashed border-slate-800 rounded-xl">
              Nessuna domanda presente. Clicca su "+ Aggiungi Domanda" per iniziare.
            </div>

            <!-- Singola Domanda -->
            <div *ngFor="let question of currentQuiz.questions; let qIdx = index" class="p-5 bg-slate-950 border border-slate-800 rounded-xl space-y-4">
              <div class="flex justify-between items-start gap-4">
                <span class="text-xs font-bold font-mono text-indigo-400 uppercase">Domanda #{{ qIdx + 1 }}</span>
                <button
                  type="button"
                  (click)="removeQuestion(qIdx)"
                  class="text-xs text-rose-400 hover:text-rose-300 transition">
                  Elimina Domanda
                </button>
              </div>

              <input
                type="text"
                [(ngModel)]="question.questionText"
                placeholder="Scrivi qui il testo della domanda..."
                class="w-full bg-slate-900 border border-slate-700 rounded-xl px-4 py-2 text-sm text-white focus:outline-none focus:border-indigo-500" />

              <!-- Opzioni di risposta -->
              <div class="space-y-2 pt-2">
                <div class="flex justify-between items-center">
                  <span class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">Opzioni di Risposta:</span>
                  <button
                    type="button"
                    (click)="addOption(question)"
                    class="text-xs text-indigo-400 hover:text-indigo-300">
                    + Aggiungi Risposta
                  </button>
                </div>

                <div *ngFor="let opt of question.options; let oIdx = index" class="flex items-center gap-3 bg-slate-900 p-2 rounded-xl border border-slate-800">
                  <input
                    type="checkbox"
                    [(ngModel)]="opt.correct"
                    title="Segna come risposta corretta"
                    class="w-4 h-4 text-emerald-600 bg-slate-950 border-slate-700 rounded cursor-pointer" />

                  <input
                    type="text"
                    [(ngModel)]="opt.optionText"
                    placeholder="Testo della risposta..."
                    class="flex-1 bg-transparent border-none text-xs text-slate-200 focus:outline-none" />

                  <span *ngIf="opt.correct" class="text-[10px] font-bold text-emerald-400 uppercase bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                    Corretta
                  </span>

                  <button
                    type="button"
                    (click)="removeOption(question, oIdx)"
                    class="text-slate-500 hover:text-rose-400 text-sm px-2">
                    ✕
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Pulsanti di Azione Finale -->
          <div class="flex flex-col sm:flex-row justify-between gap-4 pt-4 border-t border-slate-800">
            <button
              *ngIf="currentQuiz.id"
              type="button"
              (click)="deleteCurrentQuiz()"
              class="px-4 py-2.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/30 text-xs font-bold rounded-xl transition">
              Elimina Intero Quiz
            </button>
            <div class="flex gap-3 sm:ml-auto">
              <button
                type="button"
                (click)="saveQuiz()"
                [disabled]="isSaving"
                class="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-xs font-bold rounded-xl transition shadow">
                {{ isSaving ? 'Salvataggio in corso...' : 'Salva Modifiche Quiz' }}
              </button>
            </div>
          </div>

        </div>

      </div>
    </div>
  `
})
export class GestioneQuizComponent implements OnInit {
  private quizService = inject(QuizService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  courses: SimpleCourse[] = [];
  selectedCourseId: number | null = null;
  isSaving = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  currentQuiz: AdminQuizDto = {
    courseId: 0,
    title: '',
    passingScore: 60,
    questions: []
  };

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.http.get<SimpleCourse[]>(`${API_BASE_URL}/courses`, { withCredentials: true }).subscribe({
      next: (res) => {
        this.courses = res;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossibile recuperare la lista dei corsi.';
        this.cdr.detectChanges();
      }
    });
  }

  onCourseChange(): void {
    if (!this.selectedCourseId) return;

    this.clearMessages();
    this.quizService.getAdminQuizByCourse(this.selectedCourseId).subscribe({
      next: (quiz) => {
        if (quiz) {
          this.currentQuiz = quiz;
        } else {
          this.resetQuizForm(this.selectedCourseId!);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.resetQuizForm(this.selectedCourseId!);
        this.cdr.detectChanges();
      }
    });
  }

  private resetQuizForm(courseId: number): void {
    const selectedCourse = this.courses.find(c => c.id === courseId);
    this.currentQuiz = {
      courseId: courseId,
      title: selectedCourse ? `Test Finale: ${selectedCourse.title}` : 'Test Finale',
      passingScore: 60,
      questions: []
    };
  }

  addQuestion(): void {
    const newQuestion: AdminQuizQuestion = {
      questionText: '',
      options: [
        { optionText: '', correct: true },
        { optionText: '', correct: false }
      ]
    };
    this.currentQuiz.questions.push(newQuestion);
  }

  removeQuestion(index: number): void {
    this.currentQuiz.questions.splice(index, 1);
  }

  addOption(question: AdminQuizQuestion): void {
    question.options.push({ optionText: '', correct: false });
  }

  removeOption(question: AdminQuizQuestion, optIndex: number): void {
    question.options.splice(optIndex, 1);
  }

  saveQuiz(): void {
    if (!this.selectedCourseId) return;

    if (!this.currentQuiz.title.trim()) {
      this.errorMessage = 'Inserisci un titolo valido per il quiz.';
      return;
    }

    this.isSaving = true;
    this.clearMessages();

    this.quizService.saveOrUpdateQuiz(this.selectedCourseId, this.currentQuiz).subscribe({
      next: (savedQuiz) => {
        this.currentQuiz = savedQuiz;
        this.successMessage = 'Quiz salvato con successo!';
        this.isSaving = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Errore durante il salvataggio del quiz: ' + (err.error?.message || err.message);
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }

  deleteCurrentQuiz(): void {
    if (!this.currentQuiz.id) return;
    if (!confirm('Sei sicuro di voler eliminare definitivamente questo quiz?')) return;

    this.quizService.deleteQuiz(this.currentQuiz.id).subscribe({
      next: () => {
        this.successMessage = 'Quiz eliminato con successo.';
        this.resetQuizForm(this.selectedCourseId!);
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Errore durante l\'eliminazione del quiz.';
        this.cdr.detectChanges();
      }
    });
  }

  private clearMessages(): void {
    this.successMessage = null;
    this.errorMessage = null;
  }
}
