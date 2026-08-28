import { Component, inject, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';
import { NotificationService, NotificationItem } from '../../services/notification.service';
import { AsyncPipe, CommonModule } from '@angular/common';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, AsyncPipe, CommonModule],
  templateUrl: './navbar.html'
})
export class NavbarComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  private notificationService = inject(NotificationService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  currentRole$ = this.authService.userRole$;
  isMenuAperto = false;

  // Stato Notifiche
  notifications: NotificationItem[] = [];
  unreadCount = 0;
  isNotificationDropdownOpen = false;
  private pollSub?: Subscription;

  ngOnInit(): void {
    this.currentRole$.subscribe(role => {
      if (role) {
        this.fetchUnreadCount();
        // Polling ogni 30 secondi per nuove notifiche
        this.pollSub = interval(30000).subscribe(() => this.fetchUnreadCount());
      } else {
        this.notifications = [];
        this.unreadCount = 0;
        if (this.pollSub) this.pollSub.unsubscribe();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pollSub) this.pollSub.unsubscribe();
  }

  fetchUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => {
        this.unreadCount = res.unreadCount;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  toggleNotifications(): void {
    this.isNotificationDropdownOpen = !this.isNotificationDropdownOpen;
    if (this.isNotificationDropdownOpen) {
      this.notificationService.getNotifications().subscribe({
        next: (items) => {
          this.notifications = items;
          this.cdr.detectChanges();
        },
        error: () => {}
      });
    }
  }

  handleNotificationClick(notif: NotificationItem): void {
    if (!notif.read) {
      this.notificationService.markAsRead(notif.id).subscribe({
        next: () => {
          notif.read = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
          this.cdr.detectChanges();
        }
      });
    }
    this.isNotificationDropdownOpen = false;
    if (notif.link) {
      void this.router.navigateByUrl(notif.link);
    }
  }

  markAllRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
        this.unreadCount = 0;
        this.cdr.detectChanges();
      }
    });
  }

  eseguiLogout(): void {
    this.isMenuAperto = false;
    this.isNotificationDropdownOpen = false;
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
