import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CourseSummaryDto } from '../models/course';
import { VideoDto } from '../models/video';
import { API_BASE_URL } from '../config/api.config';

export interface CourseReviewDto {
  id: number;
  userId: string;
  authorName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface CourseRatingSummaryDto {
  averageRating: number;
  totalReviews: number;
  reviews: CourseReviewDto[];
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/courses`;

  private httpOptions = {
    withCredentials: true
  };

  getCourses(): Observable<CourseSummaryDto[]> {
    return this.http.get<CourseSummaryDto[]>(this.apiUrl);
  }

  getCourseDetail(courseId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${courseId}`);
  }

  getVideosByCourse(courseId: number): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/${courseId}/videos`);
  }

  // --- Recensioni e Valutazioni ---

  getCourseReviews(courseId: number): Observable<CourseRatingSummaryDto> {
    return this.http.get<CourseRatingSummaryDto>(`${this.apiUrl}/${courseId}/reviews`);
  }

  submitCourseReview(courseId: number, rating: number, comment: string): Observable<CourseReviewDto> {
    return this.http.post<CourseReviewDto>(
      `${this.apiUrl}/${courseId}/reviews`,
      { rating, comment },
      this.httpOptions
    );
  }
}
