import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ScrollFadeDirective } from '../../directives/scroll-fade.directive';

@Component({
  selector: 'app-cosa-mi-ha-portato-qui',
  standalone: true,
  imports: [RouterLink, ScrollFadeDirective],
  templateUrl: './cosa-mi-ha-portato-qui.html'
})
export class CosaMiHaPortatoQui {

}
