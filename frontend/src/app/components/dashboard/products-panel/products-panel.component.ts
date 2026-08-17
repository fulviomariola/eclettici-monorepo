import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService, Product } from '../../../services/product.service';
import { OrderService, Order, OrderStatus } from '../../../services/order.service';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-products-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './products-panel.component.html'
})
export class ProductsPanelComponent implements OnInit {
  private productService = inject(ProductService);
  private orderService = inject(OrderService);
  private cdr = inject(ChangeDetectorRef);

  productsList: Product[] = [];
  ordersList: Order[] = [];

  newProduct: Product = {
    name: '',
    description: '',
    price: 0,
    imageUrl: '',
    available: true
  };

  successMessage: string | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadProducts();
    this.loadOrders();
  }

  loadProducts(): void {
    this.productService.getPublicProducts().subscribe({
      next: (data) => {
        this.productsList = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Errore caricamento prodotti:', err)
    });
  }

  loadOrders(): void {
    this.orderService.getAllOrders().subscribe({
      next: (data) => {
        this.ordersList = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Errore caricamento ordini:', err)
    });
  }

  onCreateProduct(event?: Event): void {
    if (event) event.preventDefault();

    if (!this.newProduct.name?.trim() || this.newProduct.price <= 0) {
      this.errorMessage = 'Nome e prezzo valido sono obbligatori.';
      return;
    }

    this.productService.createProduct(this.newProduct).subscribe({
      next: (created) => {
        this.successMessage = `Prodotto "${created.name}" aggiunto allo Store!`;
        this.errorMessage = null;
        this.newProduct = { name: '', description: '', price: 0, imageUrl: '', available: true };
        this.loadProducts();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore creazione prodotto:', err);
        this.errorMessage = 'Impossibile creare il prodotto.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeleteProduct(id: string | undefined): void {
    if (!id) return;
    if (confirm('Sei sicuro di voler eliminare questo prodotto?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => this.loadProducts(),
        error: (err) => console.error('Errore eliminazione prodotto:', err)
      });
    }
  }

  onChangeOrderStatus(orderId: string | undefined, status: OrderStatus): void {
    if (!orderId) return;
    this.orderService.updateOrderStatus(orderId, status).subscribe({
      next: () => this.loadOrders(),
      error: (err) => console.error('Errore aggiornamento ordine:', err)
    });
  }

  getOrderBadgeClass(status: OrderStatus): string {
    switch (status) {
      case 'PENDING': return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
      case 'PAID': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'SHIPPED': return 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20';
      case 'CANCELLED': return 'bg-rose-500/10 text-rose-400 border-rose-500/20';
      default: return 'bg-slate-800 text-slate-300';
    }
  }
}
