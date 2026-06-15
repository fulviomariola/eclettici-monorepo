import { Routes } from '@angular/router';
import { ContactFormComponent } from './components/contact-form/contact-form';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: ContactFormComponent },          // URL: http://localhost:4200/ (Home con il form contatti)
  { path: 'register', component: RegisterComponent },    // URL: http://localhost:4200/register (Pagina di registrazione)
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivateChild: [authGuard] },
  { path: '**', redirectTo: '' }                          // Wildcard per reindirizzare le rotte errate alla home
];
