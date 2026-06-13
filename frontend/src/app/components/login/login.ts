import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, UserRegistrationDto } from '../../services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html'
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  formData: UserRegistrationDto = {
    email: '',
    password: ''
  };

  successMessage: string | null = null;
  errorMessages: string[] = [];

  onLogin(): void {
    this.successMessage = null;
    this.errorMessages = [];

    this.authService.loginUtente(this.formData).subscribe({
      next: (risposta) => {
        this.successMessage = risposta.messaggio;

        // Per il momento salviamo i dati utente nel localStorage per simulare la sessione attiva
        localStorage.setItem('user_email', risposta.email);
        localStorage.setItem('user_role', risposta.role);

        this.cdr.detectChanges();

        // Facciamo un piccolo redirect simulato o pulizia dopo 1.5 secondi
        setTimeout(() => {
          // Qui potremo reindirizzare l'utente alla dashboard del negozio o dello studente
          // per ora puliamo solo i messaggi
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 1500);
      },
      error: (errore) => {
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
