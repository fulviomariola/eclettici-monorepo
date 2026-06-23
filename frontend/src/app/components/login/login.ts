import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {AuthService, LoginDto, UserRegistrationDto} from '../../services/auth';

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

  // Tipizza il form con LoginDto: ora contiene solo ed esclusivamente ciò che serve
  formData: LoginDto = {
    email: '',
    password: ''
  };

  // Stato iniziale: password nascosta
  hidePassword = true;
  successMessage: string | null = null;
  errorMessages: string[] = [];

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  onLogin(): void {
    this.successMessage = null;
    this.errorMessages = [];

    this.authService.loginUtente(this.formData).subscribe({
      next: (risposta) => {
        this.successMessage = risposta.messaggio;

        // Per il momento salviamo i dati utente nel localStorage per simulare la sessione attiva
        localStorage.setItem('user_id', risposta.id);
        localStorage.setItem('user_email', risposta.email);
        localStorage.setItem('user_role', risposta.role); // Contiene "STUDENT" o "STORE"

        this.cdr.detectChanges();

        // Reindirizzamento immediato alla dashboard dopo 1 secondo per mostrare il banner verde
        setTimeout(() => {
          // Qui potremo reindirizzare l'utente alla dashboard del negozio o dello studente
          // per ora puliamo solo i messaggi
          this.successMessage = null;
          void this.router.navigate(["/dashboard"])
          //this.cdr.detectChanges();
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
