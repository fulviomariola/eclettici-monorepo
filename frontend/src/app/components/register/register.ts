import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService, UserRegistrationDto } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './register.html'
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  formData: UserRegistrationDto = {
    email: '',
    password: ''
  };

  successMessage: string | null = null;
  errorMessages: string[] = [];

  onRegister(): void {
    this.successMessage = null;
    this.errorMessages = [];

    this.authService.registraUtente(this.formData).subscribe({
      next: (risposta) => {
        this.successMessage = 'Registrazione completata con successo! Ora puoi effettuare il login.';
        this.resetForm();
        this.cdr.detectChanges();
      },
      error: (errore) => {
        this.errorMessages = []; // Svuota gli errori precedenti

        if (errore.status === 0) {
          this.errorMessages.push('Il server di backend non risponde. Verifica che sia avviato.');
        } else if (errore.status === 400 && errore.error && errore.error.errori) {
          // Se c'è l'array di errori multipli, lo assegniamo direttamente
          this.errorMessages = errore.error.errori;
        } else if (errore.status === 400 && errore.error && errore.error.dettaglio) {
          // Rimane attivo il fallback per errori singoli (es. email già esistente)
          this.errorMessages.push(errore.error.dettaglio);
        } else {
          this.errorMessages.push('Si è verificato un errore imprevisto.');
        }

        this.cdr.detectChanges();
      }
    });
  }

  private resetForm(): void {
    this.formData = { email: '', password: '' };
  }
}
