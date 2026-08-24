import { Pipe, PipeTransform, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Pipe({
  name: 'safeUrl',
  standalone: true
})
export class SafeUrlPipe implements PipeTransform {
  private sanitizer = inject(DomSanitizer);

  transform(youtubeId: string | null | undefined): SafeResourceUrl {
    if (!youtubeId) {
      return this.sanitizer.bypassSecurityTrustResourceUrl('');
    }
    // Costruisce l'URL completo di embed e lo rende sicuro per Angular
    const embedUrl = `https://www.youtube.com/embed/${youtubeId}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
  }
}
