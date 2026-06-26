import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth'; // Adatta il percorso
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe],
  templateUrl: './navbar.html'
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  private router = inject(Router);

  // Il ruolo dell'utente verrà letto in modo asincrono nel template html
  currentRole$ = this.authService.userRole$;

  ngOnInit(): void {}

  eseguiLogout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
