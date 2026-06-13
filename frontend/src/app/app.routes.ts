import { Routes } from '@angular/router';
import { ContactFormComponent } from './components/contact-form/contact-form';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';

export const routes: Routes = [
  { path: '', component: ContactFormComponent },          // URL: http://localhost:4200/ (Home con il form contatti)
  { path: 'register', component: RegisterComponent },    // URL: http://localhost:4200/register (Pagina di registrazione)
  { path: 'login', component: LoginComponent },
  { path: '**', redirectTo: '' }                          // Wildcard per reindirizzare le rotte errate alla home
];
