import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Fondamentale per ngModel
import { Router } from '@angular/router';
import { AuthService, UserRegistrationDto } from '../../services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html'
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  // Inizializziamo l'oggetto DTO con tutti i campi del form vuoti
  registrationData: UserRegistrationDto = {
    nome: '',
    cognome: '',
    email: '',
    password: '',
    ruolo: ''
  };

  errorMessages: string[] = [];

  onRegister(event: Event): void {
    event.preventDefault();
    this.errorMessages = []; // Reset errori precedenti

    this.authService.registraUtente(this.registrationData).subscribe({
      next: () => {
        // Registrazione completata, reindirizziamo l'utente al login
        void this.router.navigate(['/login']);
      },
      error: (errore) => {
        if (errore.status === 0) {
          this.errorMessages.push('Il server di backend non risponde. Verifica che sia avviato.');
        } else if (errore.status === 400 && errore.error && errore.error.errori) {
          // Cattura l'array completo di validazione inviato dal GlobalExceptionHandler
          this.errorMessages = errore.error.errori;
        } else if (errore.status === 400 && errore.error && errore.error.dettaglio) {
          this.errorMessages.push(errore.error.dettaglio);
        } else {
          this.errorMessages.push('Si è verificato un errore imprevisto durante la registrazione.');
        }
        this.cdr.detectChanges();
      }
    });
  }
}
