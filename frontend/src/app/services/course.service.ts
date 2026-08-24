import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CourseSummaryDto } from '../models/course';
import { VideoDto } from '../models/video';
import { API_BASE_URL } from '../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/courses`;

  getCourses(): Observable<CourseSummaryDto[]> {
    return this.http.get<CourseSummaryDto[]>(this.apiUrl);
  }

  getCourseDetail(courseId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${courseId}`);
  }

  getVideosByCourse(courseId: number): Observable<VideoDto[]> {
    return this.http.get<VideoDto[]>(`${this.apiUrl}/${courseId}/videos`);
  }
}
