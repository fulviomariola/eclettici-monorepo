import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GithubService } from '../../services/github.service';
import { RepositoryDto } from '../../models/repository';

@Component({
  selector: 'app-github',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './github.html'
})
export class GithubComponent implements OnInit {
  private githubService = inject(GithubService);
  private cdr = inject(ChangeDetectorRef);

  listaRepo: RepositoryDto[] = [];
  messaggioErrore: string | null = null;

  ngOnInit(): void {
    this.caricaProgettiGitHub();
  }

  private caricaProgettiGitHub(): void {
    this.githubService.getRepositories().subscribe({
      next: (dati) => {
        // Filtriamo per escludere eventuali fork e tenere solo i tuoi progetti reali
        this.listaRepo = dati;
        this.cdr.detectChanges(); // Svegliamo Angular al ritorno dei dati
      },
      error: (err) => {
        console.error('Errore nel recupero dei progetti GitHub:', err);
        this.messaggioErrore = 'Impossibile caricare i progetti GitHub. Riprova più tardi.';
        this.cdr.detectChanges();
      }
    });
  }
}
