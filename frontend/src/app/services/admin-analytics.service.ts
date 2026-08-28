import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface CourseMetricDto {
  courseId: number;
  courseTitle: string;
  premium: boolean;
  totalAttempts: number;
  passedAttempts: number;
  passRate: number;
  averageRating: number;
  reviewsCount: number;
}

export interface AdminAnalyticsDto {
  totalUsers: number;
  totalCourses: number;
  totalQuizAttempts: number;
  totalCertificatesIssued: number;
  overallAverageRating: number;
  courseMetrics: CourseMetricDto[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminAnalyticsService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/admin/analytics`;

  private httpOptions = {
    withCredentials: true
  };

  getAnalytics(): Observable<AdminAnalyticsDto> {
    return this.http.get<AdminAnalyticsDto>(this.apiUrl, this.httpOptions);
  }
}
