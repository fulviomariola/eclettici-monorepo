import { Component, OnInit, ViewChild, inject, ChangeDetectorRef } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { EmailPanelComponent } from './email-panel/email-panel.component';
import { ProductsPanelComponent } from './products-panel/products-panel.component';

// IMPORT SOTTO-COMPONENTI
import { StorePanelComponent } from './store-panel/store-panel.component';
import { AdminPanelComponent } from './admin-panel/admin-panel.component';
import { CommunityBoardComponent } from './community-board/community-board.component';
import { ServicesPanelComponent } from './services-panel/services-panel.component';
import { ContactsPanelComponent } from './contacts-panel/contacts-panel.component';

import { PostService, PostResponseDto } from '../../services/post';
import { VideoService } from '../../services/video.service';
import { VideoDto } from '../../models/video';

// Tipo per le schede disponibili
export type DashboardTab = 'community' | 'management' | 'store' | 'email';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    StorePanelComponent,
    AdminPanelComponent,
    CommunityBoardComponent,
    ServicesPanelComponent,
    ContactsPanelComponent,
    EmailPanelComponent,
    ProductsPanelComponent
  ],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private router = inject(Router);
  private postService = inject(PostService);
  private videoService = inject(VideoService);
  private cdr = inject(ChangeDetectorRef);

  @ViewChild(CommunityBoardComponent) communityBoard!: CommunityBoardComponent;

  userEmail: string | null = '';
  currentUserId: string = '';
  userRole: string = '';

  // Scheda attiva predefinita
  activeTab: DashboardTab = 'community';

  postsList: PostResponseDto[] = [];
  videosList: VideoDto[] = [];
  showOnlyPrivate: boolean = false;

  ngOnInit(): void {
    this.userEmail = localStorage.getItem('user_email');
    this.userRole = localStorage.getItem('user_role') || '';
    this.currentUserId = localStorage.getItem('user_id') || '';

    if (!this.userEmail || !this.currentUserId) {
      void this.router.navigate(['/login']);
    }

    if (this.userRole === 'ADMIN') {
      this.loadAdminStats();
    }
  }

  setTab(tab: DashboardTab): void {
    this.activeTab = tab;
  }

  loadAdminStats(): void {
    this.postService.getAllPosts(this.currentUserId, this.userRole).subscribe({
      next: (posts) => {
        this.postsList = posts;
        this.cdr.detectChanges();
      }
    });

    this.videoService.getVideosPremium().subscribe({
      next: (videos) => {
        this.videosList = videos;
        this.cdr.detectChanges();
      }
    });
  }

  onPostCreated(): void {
    if (this.communityBoard) {
      this.communityBoard.loadPosts();
    }
    if (this.userRole === 'ADMIN') {
      this.loadAdminStats();
    }
  }

  onVideoCreated(): void {
    if (this.userRole === 'ADMIN') {
      this.loadAdminStats();
    }
  }

  onFilterToggled(filterValue: boolean): void {
    this.showOnlyPrivate = filterValue;
  }

  onLogout(): void {
    localStorage.clear();
    void this.router.navigate(['/login']);
  }
}
