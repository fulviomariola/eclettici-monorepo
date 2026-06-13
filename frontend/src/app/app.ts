import { Component } from '@angular/core';
import { ContactFormComponent } from './components/contact-form/contact-form'; // Importa il form

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ContactFormComponent], // Registralo qui
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  title = 'frontend';
}
