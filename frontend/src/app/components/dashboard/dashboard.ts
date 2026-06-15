import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private router = inject(Router);

  userEmail: string | null = '';
  userRole: string | null = '';

  ngOnInit(): void {
    // Recuperiamo i dati della sessione dal localStorage
    this.userEmail = localStorage.getItem('user_email');
    this.userRole = localStorage.getItem('user_role');

    // Controllo di sicurezza base: se non ci sono dati, rimanda al login
    if (!this.userEmail) {
      this.router.navigate(['/login']);
    }
  }

  onLogout(): void {
    // Svuotiamo la sessione e torniamo alla home
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}
