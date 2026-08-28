import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface ProjectSubmissionDto {
  id: number;
  courseId: number;
  courseTitle: string;
  userId: string;
  studentName: string;
  studentEmail: string;
  repoUrl: string;
  notes: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  adminFeedback?: string;
  submittedAt: string;
  reviewedAt?: string;
}

export interface ProjectReviewRequestDto {
  status: 'APPROVED' | 'REJECTED';
  feedback: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/projects`;

  private httpOptions = {
    withCredentials: true
  };

  // Metodi Studente
  submitProject(courseId: number, repoUrl: string, notes: string): Observable<ProjectSubmissionDto> {
    return this.http.post<ProjectSubmissionDto>(
      `${this.apiUrl}/course/${courseId}/submit`,
      { repoUrl, notes },
      this.httpOptions
    );
  }

  getMySubmission(courseId: number): Observable<ProjectSubmissionDto> {
    return this.http.get<ProjectSubmissionDto>(
      `${this.apiUrl}/course/${courseId}/my-submission`,
      this.httpOptions
    );
  }

  // Metodi Amministratore
  getAllSubmissions(): Observable<ProjectSubmissionDto[]> {
    return this.http.get<ProjectSubmissionDto[]>(`${this.apiUrl}/admin/all`, this.httpOptions);
  }

  reviewSubmission(submissionId: number, payload: ProjectReviewRequestDto): Observable<ProjectSubmissionDto> {
    return this.http.patch<ProjectSubmissionDto>(
      `${this.apiUrl}/admin/${submissionId}/review`,
      payload,
      this.httpOptions
    );
  }
}
