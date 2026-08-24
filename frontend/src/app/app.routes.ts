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

  // Rotta riservata esclusivamente ad ADMIN
  {
    path: 'gestione-video',
    component: GestioneVideoComponent,
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] } // <-- Aggiunto vincolo di ruolo
  },

  { path: 'cosa-mi-ha-portato-qui', component: CosaMiHaPortatoQui },
  { path: 'videolezioni', component: CatalogoCorsiComponent },
  { path: 'videolezioni/:courseId', component: VideolezioniComponent },
  { path: '**', redirectTo: '' }
];
