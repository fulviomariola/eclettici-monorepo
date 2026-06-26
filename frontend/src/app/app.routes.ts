import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { authGuard } from './guards/auth.guard';
import { YoutubeComponent } from './components/youtube/youtube';
import { GithubComponent } from './components/github/github';

export const routes: Routes = [
  { path: '', component: HomeComponent },          // URL: http://localhost:4200/ (Home con il form home)
  { path: 'register', component: RegisterComponent },    // URL: http://localhost:4200/register (Pagina di registrazione)
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'youtube', component: YoutubeComponent },
  { path: 'github', component: GithubComponent },
  { path: '**', redirectTo: '' }                          // Wildcard per reindirizzare le rotte errate alla home
];
