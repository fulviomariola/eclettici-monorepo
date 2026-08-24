import { Routes } from '@angular/router';
import { HomeComponent } from './components/home/home';
import { RegisterComponent } from './components/register/register';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { ProfileComponent } from './components/profile/profile.component';
import { authGuard } from './guards/auth.guard';
import { YoutubeComponent } from './components/youtube/youtube';
import { GithubComponent } from './components/github/github';
import { GestioneVideoComponent } from './components/gestione-video/gestione-video';
import { CatalogoCorsiComponent } from './components/catalogo-corsi/catalogo-corsi';
import { VideolezioniComponent } from './components/videolezioni/videolezioni';
import { CosaMiHaPortatoQui } from './components/cosaMiHaPortatoQui/cosa-mi-ha-portato-qui';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'youtube', component: YoutubeComponent },
  { path: 'github', component: GithubComponent },
  { path: 'gestione-video', component: GestioneVideoComponent, canActivate: [authGuard] },
  { path: 'cosa-mi-ha-portato-qui', component: CosaMiHaPortatoQui },

  // Livello 1: Catalogo a griglia dei Corsi
  { path: 'videolezioni', component: CatalogoCorsiComponent },

  // Livello 2: Aula Virtuale con player e lezioni del singolo corso
  { path: 'videolezioni/:courseId', component: VideolezioniComponent },

  { path: '**', redirectTo: '' }
];
