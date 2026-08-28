import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

// --- Interfacce per la visualizzazione Studente ---
export interface QuizOption {
  id: number;
  optionText: string;
}

export interface QuizQuestion {
  id: number;
  questionText: string;
  options: QuizOption[];
}

export interface QuizDto {
  id: number;
  title: string;
  passingScore: number;
  questions: QuizQuestion[];
}

export interface QuizResultDto {
  score: number;
  passed: boolean;
  passingScore: number;
  message: string;
}

export interface UserCertificateDto {
  attemptId: number;
  courseId: number;
  courseTitle: string;
  score: number;
  issuedAt: string;
}

export interface CertificateVerifyDto {
  attemptId: number;
  studentName: string;
  courseTitle: string;
  score: number;
  passed: boolean;
  issuedAt: string;
}

// --- Interfacce per la Gestione Amministratore (Backoffice) ---
export interface AdminQuizOption {
  id?: number;
  optionText: string;
  correct: boolean;
}

export interface AdminQuizQuestion {
  id?: number;
  questionText: string;
  options: AdminQuizOption[];
}

export interface AdminQuizDto {
  id?: number;
  courseId: number;
  courseTitle?: string;
  title: string;
  passingScore: number;
  questions: AdminQuizQuestion[];
}

@Injectable({
  providedIn: 'root'
})
export class QuizService {
  private http = inject(HttpClient);
  private apiUrl = `${API_BASE_URL}/quizzes`;
  private certificateUrl = `${API_BASE_URL}/certificates`;

  private httpOptions = {
    withCredentials: true
  };

  // --- Metodi Studente ---

  getQuizByCourse(courseId: number): Observable<QuizDto> {
    return this.http.get<QuizDto>(`${this.apiUrl}/course/${courseId}`);
  }

  submitQuiz(quizId: number, answers: { questionId: number; selectedOptionId: number }[]): Observable<QuizResultDto> {
    return this.http.post<QuizResultDto>(`${this.apiUrl}/${quizId}/submit`, { answers }, this.httpOptions);
  }

  scaricaCertificato(courseId: number): Observable<Blob> {
    return this.http.get(`${this.certificateUrl}/course/${courseId}/download`, {
      responseType: 'blob',
      withCredentials: true
    });
  }

  getUserCertificates(): Observable<UserCertificateDto[]> {
    return this.http.get<UserCertificateDto[]>(`${this.certificateUrl}/my-certificates`, this.httpOptions);
  }

  verifyCertificate(attemptId: number): Observable<CertificateVerifyDto> {
    return this.http.get<CertificateVerifyDto>(`${this.certificateUrl}/verify/${attemptId}`);
  }

  // --- Metodi Amministratore (Backoffice) ---

  getAllAdminQuizzes(): Observable<AdminQuizDto[]> {
    return this.http.get<AdminQuizDto[]>(`${this.apiUrl}/admin/all`, this.httpOptions);
  }

  getAdminQuizByCourse(courseId: number): Observable<AdminQuizDto> {
    return this.http.get<AdminQuizDto>(`${this.apiUrl}/admin/course/${courseId}`, this.httpOptions);
  }

  saveOrUpdateQuiz(courseId: number, quizData: AdminQuizDto): Observable<AdminQuizDto> {
    return this.http.post<AdminQuizDto>(`${this.apiUrl}/admin/course/${courseId}`, quizData, this.httpOptions);
  }

  deleteQuiz(quizId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/${quizId}`, this.httpOptions);
  }
}
