import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ContactService, ContactMessage, ContactMessageStatus } from '../../../services/contact.service';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-contacts-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './contacts-panel.component.html'
})
export class ContactsPanelComponent implements OnInit {
  private contactService = inject(ContactService);
  private cdr = inject(ChangeDetectorRef);

  messagesList: ContactMessage[] = [];
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadMessages();
  }

  loadMessages(): void {
    this.contactService.getAllMessages().subscribe({
      next: (data) => {
        this.messagesList = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore caricamento messaggi contatti:', err);
        this.errorMessage = 'Impossibile caricare le richieste di contatto.';
        this.cdr.detectChanges();
      }
    });
  }

  onChangeStatus(id: string, newStatus: ContactMessageStatus): void {
    this.contactService.updateStatus(id, newStatus).subscribe({
      next: () => {
        this.loadMessages();
      },
      error: (err) => {
        console.error('Errore aggiornamento stato:', err);
        this.errorMessage = 'Impossibile aggiornare lo stato della richiesta.';
        this.cdr.detectChanges();
      }
    });
  }

  getStatusBadgeClass(status: ContactMessageStatus): string {
    switch (status) {
      case 'NEW':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
      case 'IN_PROGRESS':
        return 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20';
      case 'COMPLETED':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'ARCHIVED':
        return 'bg-slate-700/50 text-slate-400 border-slate-600';
      default:
        return 'bg-slate-800 text-slate-300';
    }
  }
}
