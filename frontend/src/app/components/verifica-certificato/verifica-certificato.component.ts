import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { QuizService, CertificateVerifyDto } from '../../services/quiz.service';

@Component({
  selector: 'app-verifica-certificato',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen bg-slate-950 text-white flex items-center justify-center p-4">
      <div class="max-w-md w-full bg-slate-900 border border-slate-800 rounded-2xl p-6 sm:p-8 space-y-6 shadow-2xl text-center">

        @if (isLoading) {
          <div class="text-indigo-400 font-medium text-sm animate-pulse">
            Verifica autenticità attestato in corso...
          </div>
        } @else if (certificateInfo) {
          <div class="w-16 h-16 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded-full flex items-center justify-center text-3xl mx-auto">
            ✓
          </div>

          <div class="space-y-2">
            <span class="text-[10px] font-mono tracking-widest text-emerald-400 uppercase bg-emerald-500/10 px-2.5 py-1 rounded-full border border-emerald-500/20">
              Certificato Ufficiale
            </span>
            <h2 class="text-xl font-bold text-white pt-2">{{ certificateInfo.studentName }}</h2>
            <p class="text-xs text-slate-400">ha completato con successo il percorso formativo</p>
            <h3 class="text-base font-bold text-indigo-400">"{{ certificateInfo.courseTitle }}"</h3>
          </div>

          <div class="p-4 bg-slate-950/60 rounded-xl border border-slate-800 text-xs space-y-2 text-slate-300 text-left">
            <div class="flex justify-between">
              <span class="text-slate-400">Punteggio Esame:</span>
              <strong class="text-emerald-400">{{ certificateInfo.score }}%</strong>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-400">Data Rilascio:</span>
              <span>{{ certificateInfo.issuedAt | date:'dd/MM/yyyy' }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-400">Identificativo Univoco:</span>
              <span class="font-mono text-indigo-400">#{{ certificateInfo.attemptId }}</span>
            </div>
          </div>

          <a routerLink="/" class="block w-full py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold rounded-xl transition no-underline shadow-md">
            Torna alla Home
          </a>
        } @else {
          <div class="text-4xl">❌</div>
          <h2 class="text-lg font-bold text-rose-400">Attestato Non Riconosciuto</h2>
          <p class="text-xs text-slate-400">Il codice identificativo fornito non corrisponde ad alcun certificato valido emesso dal sistema.</p>
          <a routerLink="/" class="block w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold rounded-xl no-underline transition">
            Torna alla Home
          </a>
        }

      </div>
    </div>
  `
})
export class VerificaCertificatoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private quizService = inject(QuizService);

  isLoading = true;
  certificateInfo: CertificateVerifyDto | null = null;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('attemptId');
    if (idParam) {
      this.quizService.verifyCertificate(Number(idParam)).subscribe({
        next: (res) => {
          this.certificateInfo = res;
          this.isLoading = false;
        },
        error: () => {
          this.certificateInfo = null;
          this.isLoading = false;
        }
      });
    } else {
      this.isLoading = false;
    }
  }
}
