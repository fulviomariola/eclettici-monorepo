import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

export interface Product {
  id?: string;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  available: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = `${API_BASE_URL}/products`;

  constructor(private http: HttpClient) {}

  getPublicProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  updateProduct(id: string, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
