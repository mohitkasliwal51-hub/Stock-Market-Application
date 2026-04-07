import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export type OrderType = 'MARKET' | 'LIMIT' | 'STOP_LOSS' | 'TAKE_PROFIT' | 'TRAILING_STOP';
export type OrderSide = 'BUY' | 'SELL';

export interface Order {
  id: number;
  userId: number;
  portfolioId: number;
  stockId: number;
  quantity: number;
  idempotencyKey: string;
  orderType: OrderType;
  side: OrderSide;
  status: string;
  orderPrice: number | null;
  triggerPrice: number | null;
  trailAmount: number | null;
  referencePrice: number | null;
  reservedAmount: number | null;
  executedPrice: number | null;
  createdAt: string;
  executedAt: string | null;
}

export interface OrderExecution {
  id: number;
  eventType: string;
  marketPrice: number | null;
  referencePrice: number | null;
  note: string;
  eventAt: string;
}

export interface CreateOrderRequest {
  userId: number;
  portfolioId: number;
  stockId: number;
  quantity: number;
  orderType: OrderType;
  side: OrderSide;
  idempotencyKey: string;
  orderPrice?: number;
  triggerPrice?: number;
  trailAmount?: number;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly apiUrl = `${environment.apiURL}/api/orders`;

  constructor(private httpClient: HttpClient) {
  }

  createOrder(payload: CreateOrderRequest): Observable<Order> {
    return this.httpClient.post<Order>(this.apiUrl, payload);
  }

  listOrders(userId: number): Observable<Order[]> {
    return this.httpClient.get<Order[]>(`${this.apiUrl}?userId=${userId}`);
  }

  cancelOrder(orderId: number): Observable<Order> {
    return this.httpClient.post<Order>(`${this.apiUrl}/${orderId}/cancel`, {});
  }

  evaluatePendingOrders(): Observable<{ executed: number; status: string }> {
    return this.httpClient.post<{ executed: number; status: string }>(`${this.apiUrl}/evaluate`, {});
  }

  getExecutionHistory(orderId: number): Observable<OrderExecution[]> {
    return this.httpClient.get<OrderExecution[]>(`${this.apiUrl}/${orderId}/executions`);
  }
}
