import { Component, Input, Output, EventEmitter, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostResponseDto } from '../../../services/post';
import { VideoDto } from '../../../models/video';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';
import { AdminAnalyticsService, AdminAnalyticsDto } from '../../../services/admin-analytics.service';
import { ProjectService, ProjectSubmissionDto } from '../../../services/project.service';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './admin-panel.component.html'
})
export class AdminPanelComponent implements OnInit {
  private analyticsService = inject(AdminAnalyticsService);
  private projectService = inject(ProjectService);
  private cdr = inject(ChangeDetectorRef);

  @Input({ required: true }) postsList: PostResponseDto[] = [];
  @Input({ required: true }) videosList: VideoDto[] = [];
  @Input() showOnlyPrivate: boolean = false;

  @Output() filterToggled = new EventEmitter<boolean>();

  analytics: AdminAnalyticsDto | null = null;
  isLoadingAnalytics = true;

  // Gestione Consegne Progetti
  projectSubmissions: ProjectSubmissionDto[] = [];
  isLoadingProjects = true;
  reviewFeedbackMap: { [submissionId: number]: string } = {};

  ngOnInit(): void {
    this.caricaAnalytics();
    this.caricaProgetti();
  }

  caricaAnalytics(): void {
    this.isLoadingAnalytics = true;
    this.analyticsService.getAnalytics().subscribe({
      next: (data) => {
        this.analytics = data;
        this.isLoadingAnalytics = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore recupero analytics:', err);
        this.isLoadingAnalytics = false;
        this.cdr.detectChanges();
      }
    });
  }

  caricaProgetti(): void {
    this.isLoadingProjects = true;
    this.projectService.getAllSubmissions().subscribe({
      next: (subs) => {
        this.projectSubmissions = subs;
        subs.forEach(s => {
          if (s.adminFeedback) {
            this.reviewFeedbackMap[s.id] = s.adminFeedback;
          }
        });
        this.isLoadingProjects = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore recupero progetti:', err);
        this.isLoadingProjects = false;
        this.cdr.detectChanges();
      }
    });
  }

  valutaProgetto(submission: ProjectSubmissionDto, status: 'APPROVED' | 'REJECTED'): void {
    const feedback = this.reviewFeedbackMap[submission.id] || '';

    this.projectService.reviewSubmission(submission.id, { status, feedback }).subscribe({
      next: (updated) => {
        submission.status = updated.status;
        submission.adminFeedback = updated.adminFeedback;
        submission.reviewedAt = updated.reviewedAt;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Errore durante la revisione:', err)
    });
  }

  get privatePostsCount(): number {
    return this.postsList.filter(p => p.isPrivate).length;
  }

  togglePrivateFilter(): void {
    this.showOnlyPrivate = !this.showOnlyPrivate;
    this.filterToggled.emit(this.showOnlyPrivate);
  }
}
