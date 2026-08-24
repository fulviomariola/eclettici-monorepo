import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginDto } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html'
})
export class LoginComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);

  formData: LoginDto = {
    email: '',
    password: ''
  };

  hidePassword = true;
  isLoading = false;
  successMessage: string | null = null;
  errorMessages: string[] = [];

  // Destinazione di default se nessun returnUrl viene fornito
  private returnUrl: string = '/dashboard';

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const emailRicevuta = params['email'];
      if (emailRicevuta) {
        this.formData.email = emailRicevuta;
      }

      // Intercetta l'URL di ritorno dai parametri della query
      if (params['returnUrl']) {
        this.returnUrl = params['returnUrl'];
      }

      this.cdr.detectChanges();
    });
  }

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  onLogin(): void {
    if (this.isLoading) return;

    this.isLoading = true;
    this.successMessage = null;
    this.errorMessages = [];

    this.authService.loginUtente(this.formData).subscribe({
      next: (risposta) => {
        this.successMessage = risposta.messaggio;

        // Salvataggio token e sessione
        localStorage.setItem('token', risposta.token);
        localStorage.setItem('user_id', risposta.id);
        localStorage.setItem('user_email', risposta.email);
        localStorage.setItem('user_role', risposta.role);

        this.authService.aggiornaStatoSessione();
        this.cdr.detectChanges();

        // Reindirizzamento dinamico a returnUrl (/videolezioni o /dashboard)
        setTimeout(() => {
          this.successMessage = null;
          void this.router.navigateByUrl(this.returnUrl);
        }, 1500);
      },
      error: (errore) => {
        this.isLoading = false;

        if (errore.status === 0) {
          this.errorMessages.push('Il server di backend non risponde. Verifica che sia avviato.');
        } else if (errore.status === 401) {
          this.errorMessages.push('Credenziali non valide. Controlla email e password.');
        } else {
          this.errorMessages.push('Si è verificato un errore imprevisto durante il login.');
        }
        this.cdr.detectChanges();
      }
    });
  }
}
