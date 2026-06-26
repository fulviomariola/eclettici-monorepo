import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {AuthService, LoginDto, UserRegistrationDto} from '../../services/auth';

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

  // Tipizza il form con LoginDto: ora contiene solo ed esclusivamente ciò che serve
  formData: LoginDto = {
    email: '',
    password: ''
  };

  // Stato iniziale: password nascosta
  hidePassword = true;
  successMessage: string | null = null;
  errorMessages: string[] = [];

  ngOnInit():void {
    this.route.queryParams.subscribe(params => {
      const emailRicevuta = params['email'];
      if (emailRicevuta) {
        this.formData.email = emailRicevuta;
        this.cdr.detectChanges();     // Forza il rendering per mostrare subito il testo nell'input
      }
    });
  }

  togglePasswordVisibility(): void {
    this.hidePassword = !this.hidePassword;
  }

  onLogin(): void {
    this.successMessage = null;
    this.errorMessages = [];

    this.authService.loginUtente(this.formData).subscribe({
      next: (risposta) => {
        this.successMessage = risposta.messaggio;

        // Salvare JWT ufficiale
        localStorage.setItem('token', risposta.token);

        // Per il momento salviamo i dati utente nel localStorage per simulare la sessione attiva
        localStorage.setItem('user_id', risposta.id);
        localStorage.setItem('user_email', risposta.email);
        localStorage.setItem('user_role', risposta.role); // Contiene "STUDENT" o "STORE"

        this.authService.aggiornaStatoSessione();
        this.cdr.detectChanges();

        // Reindirizzamento immediato alla dashboard dopo 1,5 secondi per mostrare il banner verde
        setTimeout(() => {
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
