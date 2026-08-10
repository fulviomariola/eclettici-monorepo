import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UserService, UserProfileDto, ChangePasswordDto } from '../../services/user.service';
import { ScrollFadeDirective } from '../../directives/scroll-fade.directive';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  private userService = inject(UserService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  userProfile: UserProfileDto | null = null;

  passwordData: ChangePasswordDto = {
    currentPassword: '',
    newPassword: ''
  };

  confirmPassword: string = '';

  successMessage: string | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.userService.getProfile().subscribe({
      next: (profile) => {
        this.userProfile = profile;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento del profilo:', err);
        this.errorMessage = 'Impossibile caricare i dati del profilo.';
        this.cdr.detectChanges();
      }
    });
  }

  onChangePassword(event?: Event): void {
    if (event) event.preventDefault();

    if (!this.passwordData.currentPassword || !this.passwordData.newPassword) {
      this.errorMessage = 'Compila tutti i campi password.';
      return;
    }

    if (this.passwordData.newPassword !== this.confirmPassword) {
      this.errorMessage = 'La Nuova Password e la Conferma Nuova Passowrd non coincidono.';
      return;
    }

    if (this.passwordData.newPassword.length < 6) {
      this.errorMessage = 'La nuova password deve contenere almeno 6 caratteri.';
      return;
    }

    this.userService.changePassword(this.passwordData).subscribe({
      next: (res) => {
        this.successMessage = res.message || 'Password modificata con successo!';
        this.errorMessage = null;
        this.passwordData = { currentPassword: '', newPassword: '' };
        this.confirmPassword = '';
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore durante il cambio password:', err);
        this.errorMessage = err.error?.message || 'Errore durante l\'aggiornamento della password.';
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    void this.router.navigate(['/dashboard']);
  }
}
