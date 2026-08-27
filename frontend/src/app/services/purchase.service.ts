import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PurchaseService {
  private http = inject(HttpClient);
  private apiUrl = '/api/purchases';

  checkPurchase(courseId: number): Observable<{ purchased: boolean }> {
    return this.http.get<{ purchased: boolean }>(`${this.apiUrl}/check/${courseId}`);
  }

  buyCourse(courseId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/course/${courseId}`, {});
  }
}
