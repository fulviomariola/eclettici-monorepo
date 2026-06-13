import { Component, inject, ChangeDetectorRef } from '@angular/core';  // IMPORTAZIONE DI ChangeDetectorRef
import { FormsModule } from '@angular/forms';
// Modificato il percorso: punta a 'contact' anziché 'contact.service'
import { ContactService, ContactRequestDto } from '../../services/contact';

@Component({
  selector: 'app-contact-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './contact-form.html',
  styleUrl: './contact-form.css'
})

export class ContactFormComponent {
  private contactService = inject(ContactService);
  private cdr = inject(ChangeDetectorRef);

  formData: ContactRequestDto = {
    name: '',
    companyName: '',
    email: '',
    phone: '',
    message: ''
  };

  successMessage: string | null = null;

  // 1. TRASFORMIAMO LA VARIABILE IN UN ARRAY DI STRINGHE
  errorMessages: string[] = [];

  onSubmit(): void {
    this.successMessage = null;
    this.errorMessages = []; // Svuotiamo l'array prima di ogni invio

    this.contactService.inviaRichiesta(this.formData).subscribe({
      next: (risposta) => {
        this.successMessage = 'Richiesta inviata con successo! Ti ricontatteremo a breve.';
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
    this.formData = { name: '', companyName: '', email: '', phone: '', message: '' };
  }
}
