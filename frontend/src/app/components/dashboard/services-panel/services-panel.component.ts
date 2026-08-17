import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ServiceOfferService, ServiceOffer } from '../../../services/service-offer.service';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-services-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './services-panel.component.html'
})
export class ServicesPanelComponent implements OnInit {
  private serviceOfferService = inject(ServiceOfferService);
  private cdr = inject(ChangeDetectorRef);

  servicesList: ServiceOffer[] = [];

  newService: ServiceOffer = {
    title: '',
    description: '',
    iconName: 'code',
    active: true
  };

  editingServiceId: string | null = null;
  editServiceData: ServiceOffer = { title: '', description: '', iconName: '', active: true };

  successMessage: string | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.serviceOfferService.getPublicServices().subscribe({
      next: (data) => {
        this.servicesList = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei servizi:', err);
        this.errorMessage = 'Impossibile caricare i servizi.';
        this.cdr.detectChanges();
      }
    });
  }

  onCreateService(event?: Event): void {
    if (event) event.preventDefault();

    if (!this.newService.title?.trim() || !this.newService.description?.trim()) {
      this.errorMessage = 'Titolo e descrizione sono obbligatori.';
      return;
    }

    this.serviceOfferService.createService(this.newService).subscribe({
      next: (created) => {
        this.successMessage = `Servizio "${created.title}" creato con successo!`;
        this.errorMessage = null;
        this.newService = { title: '', description: '', iconName: 'code', active: true };
        this.loadServices();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore creazione servizio:', err);
        this.errorMessage = 'Impossibile creare il servizio. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  onStartEdit(service: ServiceOffer): void {
    if (!service.id) return;
    this.editingServiceId = service.id;
    this.editServiceData = {
      title: service.title,
      description: service.description,
      iconName: service.iconName || '',
      active: service.active
    };
  }

  onCancelEdit(): void {
    this.editingServiceId = null;
    this.editServiceData = { title: '', description: '', iconName: '', active: true };
  }

  onSaveEdit(serviceId: string | undefined): void {
    if (!serviceId) return;

    this.serviceOfferService.updateService(serviceId, this.editServiceData).subscribe({
      next: () => {
        this.successMessage = 'Servizio aggiornato con successo!';
        this.editingServiceId = null;
        this.loadServices();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore modifica servizio:', err);
        this.errorMessage = 'Impossibile modificare il servizio.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeleteService(serviceId: string | undefined): void {
    if (!serviceId) return;

    if (confirm('Sei sicuro di voler eliminare questo servizio?')) {
      this.serviceOfferService.deleteService(serviceId).subscribe({
        next: () => {
          this.loadServices();
        },
        error: (err) => {
          console.error('Errore eliminazione servizio:', err);
          this.errorMessage = 'Impossibile eliminare il servizio.';
          this.cdr.detectChanges();
        }
      });
    }
  }
}
