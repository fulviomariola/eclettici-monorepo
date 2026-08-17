import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export type OrderStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'CANCELLED';

export interface Order {
  id?: string;
  userId: string;
  totalAmount: number;
  status: OrderStatus;
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${API_BASE_URL}/orders`;

  constructor(private http: HttpClient) {}

  createOrder(order: Order): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order);
  }

  getUserOrders(userId: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/user/${userId}`);
  }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  updateOrderStatus(id: string, status: OrderStatus): Observable<Order> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Order>(`${this.apiUrl}/${id}/status`, {}, { params });
  }
}
