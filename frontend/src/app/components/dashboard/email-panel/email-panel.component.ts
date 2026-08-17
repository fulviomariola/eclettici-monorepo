import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmailService, BulkEmailRequest } from '../../../services/email.service';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-email-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './email-panel.component.html'
})
export class EmailPanelComponent {
  private emailService = inject(EmailService);
  private cdr = inject(ChangeDetectorRef);

  emailData: BulkEmailRequest = {
    target: 'ALL_SUBSCRIBERS',
    subject: '',
    body: ''
  };

  successMessage: string | null = null;
  errorMessage: string | null = null;
  isSending = false;

  onSendBulkEmail(event?: Event): void {
    if (event) event.preventDefault();

    if (!this.emailData.subject.trim() || !this.emailData.body.trim()) {
      this.errorMessage = 'Oggetto e testo della mail sono obbligatori.';
      return;
    }

    this.isSending = true;
    this.errorMessage = null;

    this.emailService.sendBulkEmail(this.emailData).subscribe({
      next: (response) => {
        this.successMessage = response || 'Invio avviato con successo in background!';
        this.isSending = false;
        this.emailData.subject = '';
        this.emailData.body = '';
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore invio email:', err);
        this.errorMessage = 'Impossibile avviare l\'invio. Verificare le credenziali o i permessi.';
        this.isSending = false;
        this.cdr.detectChanges();
      }
    });
  }
}
