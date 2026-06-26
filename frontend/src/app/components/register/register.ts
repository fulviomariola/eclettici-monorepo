import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import { AuthService, UserRegistrationDto } from '../../services/auth';
import {readonly} from '@angular/forms/signals';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.html'
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  private emailTimeout: any;

  emailDuplicata = false;
  emailInserita = '';

  // DTO ufficiale per il backend
  registrationData: UserRegistrationDto = {
    nome: '',
    cognome: '',
    email: '',
    password: '',
    ruolo: 'STUDENT' // Impostiamo un valore di default sicuro
  };

  // Proprietà locali per la UX (non vengono inviate al server)
  confermaPassword: string = '';
  hidePassword: boolean = true;
  readonly MIN_PASSWORD_LENGTH = 6;   // vincolo di min 6 caratteri
  caratteriMancanti: number = this.MIN_PASSWORD_LENGTH;   // tracciamento caratteri mancanti
  lunghezzaValida: boolean = false;
  secondoCampoToccato: boolean = false;
  passwordCoincidono: boolean = false;
  haCarattereSpeciale: boolean = false;

  // Supponiamo che tu stia usando i Reactive Forms (FormGroup)
  // o i Template-driven forms. Ecco la funzione da attivare sull'evento dell'input:
  verificaEmail(email: string): void {
    // 1. Puliamo il vecchio timer se l'utente sta ancora digitando
    if (this.emailTimeout) {
      clearTimeout(this.emailTimeout);
    }

    // Valutazione preliminare base
    if (!email || !email.includes('@') || !email.includes('.')) {
      this.emailDuplicata = false;
      return;
    }

    this.emailInserita = email;

    // 2. Facciamo partire il controllo solo se l'utente si ferma per 450 millisecondi
    this.emailTimeout = setTimeout(() => {
      this.authService.checkEmail(email).subscribe({
        next: (esiste) => {
          // Se esiste è true, attiviamo il blocco visivo
          this.emailDuplicata = esiste;

          // Se l'email è duplicata, possiamo precompilare il DTO parzialmente o svuotarlo
          if (esiste) {
            console.log('Attenzione: Email già presente nel DB. Mostrare messaggio empatico.');
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Errore durante il controllo email', err);
        }
      });
    }, 450);
  }

  verificaPassword(): void {
    // 1. Controlo lunghezza password principale
    const attualeLunghezza = this.registrationData.password ? this.registrationData.password.length : 0;

    // Calcolo caratteri che mancano (sopra lo 0)
    this.caratteriMancanti = Math.max(0, this.MIN_PASSWORD_LENGTH - attualeLunghezza);
    this.lunghezzaValida = attualeLunghezza >= this.MIN_PASSWORD_LENGTH;

    // --- AGGIUNGI QUESTA RIGA PER IL CONTROLLO DINAMICO DEL CARATTERE SPECIALE ---
    const regexSpeciali = /[$&%@#!*?]/;
    this.haCarattereSpeciale = regexSpeciali.test(this.registrationData.password || '');
    // -----------------------------------------------------------------------------

    // 2. Controllo match (solo se user ha scritto nel secondo campo
    if (this.confermaPassword && this.confermaPassword.length > 0) {
      this.secondoCampoToccato = true;
    }

    // Se hai toccato secondo campo, verifica uguaglianza
    if (this.secondoCampoToccato) {
      this.passwordCoincidono = this.registrationData.password === this.confermaPassword;
    }
  }

  errorMessages: string[] = [];

  // Metodo per invertire la visibilità della password nell'HTML
  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  onRegister(event: Event): void {
    event.preventDefault();
    this.errorMessages = []; // Reset errori precedenti

    // 1. Usiamo il flag dinamico che si aggiorna mentre l'utente digita
    if (!this.passwordCoincidono) {
      this.errorMessages.push('Le password inserite non coincidono.');
      return;
    }

    // 2. Controllo di lunghezza minima locale prima dell'invio
    if (!this.lunghezzaValida) {
      this.errorMessages.push(`La password deve contenere almeno ${this.MIN_PASSWORD_LENGTH} caratteri.`);
      return;
    }

    // Controllo caratteri speciale
    if (!this.haCarattereSpeciale) {
      this.errorMessages.push('La password deve contenere almeno un carattere speciale.');
      return;
    }

    // 3. Invio del DTO pulito tramite il servizio
    this.authService.registraUtente(this.registrationData).subscribe({
      next: () => {
        void this.router.navigate(['/login']);
      },
      error: (errore) => {
        if (errore.status === 0) {
          this.errorMessages.push('Il server di backend non risponde. Verifica che sia avviato.');
        } else if (errore.status === 400 && errore.error && errore.error.errori) {
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
