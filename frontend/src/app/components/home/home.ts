import {Component, ElementRef, inject, OnDestroy, OnInit} from '@angular/core';
import { RouterLink } from '@angular/router';
import { ContactFormComponent } from '../contact-form/contact-form';    // Controlla il percorso esatto

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ContactFormComponent],  // Importiamo il form dei contatti per usarlo nel template
  templateUrl: './home.html'
})
export class HomeComponent implements OnInit, OnDestroy {
  private el = inject(ElementRef);
  private observer: IntersectionObserver | null = null;

  // constructor() {}
  ngOnInit(): void {
    this.inizializzaAnimazioneMosh();
  }

  private inizializzaAnimazioneMosh(): void {
    // Configurazione dell'Observer: intercetta l'elemento quando è visibile al 15%
    const opzioni = {
      root: null,
      thresold: 0.15
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
       if (entry.isIntersecting) {
         // Quando l'elemento entra nello schermo, aggiungiamo la classe che lo mostra
         entry.target.classList.add('opacity-100', 'translate-y-0');
         entry.target.classList.remove('opacity-0', 'translate-y-10');
       } else {
         // --- IL TUO TOCCO PERSONALIZZATO ---
         // Rimuovendo la classe quando esce dallo schermo, l'animazione si ripeterà OGNI VOLTA!
         entry.target.classList.add('opacity-0', 'translate-y-10');
         entry.target.classList.remove('opacity-100', 'translate-y-0');
       }
      });
    }, opzioni);

    // Cerchiamo nell'HTML tutte le schede che devono fare il gioco di apparizione
    const elementiDaAnimare = this.el.nativeElement.querySelectorAll('.mosh-fade');
    elementiDaAnimare.forEach((elemento: HTMLElement) => {
      this.observer?.observe(elemento);
    });
  }

  ngOnDestroy(): void {
    // Puliamo l'observer quando cambiamo pagina per non sprecare memoria RAM
    if (this.observer) {
      this.observer.disconnect();
    }
  }
}














