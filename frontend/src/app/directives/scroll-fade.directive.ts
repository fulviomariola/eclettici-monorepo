import { Directive, ElementRef, inject, OnDestroy, OnInit, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appScrollFade]',
  standalone: true
})
export class ScrollFadeDirective implements OnInit, OnDestroy {
  private el = inject(ElementRef);
  private renderer = inject(Renderer2);
  private observer: IntersectionObserver | null = null;

  ngOnInit(): void {
    const element = this.el.nativeElement;

    // 1. Applichiamo automaticamente lo stato iniziale e le classi di transizione Tailwind
    this.renderer.addClass(element, 'opacity-0');
    this.renderer.addClass(element, 'translate-y-10');
    this.renderer.addClass(element, 'transition');
    this.renderer.addClass(element, 'duration-700');
    this.renderer.addClass(element, 'ease-out');

    // 2. Configurazione dell'Observer
    const opzioni = {
      root: null,
      threshold: 0.15
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          // Quando entra nello schermo: visibile e in posizione originale
          this.renderer.addClass(element, 'opacity-100');
          this.renderer.addClass(element, 'translate-y-0');
          this.renderer.removeClass(element, 'opacity-0');
          this.renderer.removeClass(element, 'translate-y-10');
        } else {
          // Quando esce dallo schermo: di nuovo nascosto e traslato
          this.renderer.addClass(element, 'opacity-0');
          this.renderer.addClass(element, 'translate-y-10');
          this.renderer.removeClass(element, 'opacity-100');
          this.renderer.removeClass(element, 'translate-y-0');
        }
      });
    }, opzioni);

    this.observer.observe(element);
  }

  ngOnDestroy(): void {
    if (this.observer) {
      this.observer.disconnect();
    }
  }
}
