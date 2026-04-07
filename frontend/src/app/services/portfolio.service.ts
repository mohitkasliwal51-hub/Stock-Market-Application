import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface PortfolioPosition {
  id: number;
  stockId: number;
  quantity: number;
  averageBuyPrice: number;
  createdAt: string;
}

export interface Portfolio {
  id: number;
  userId: number;
  portfolioName: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  positions: PortfolioPosition[];
}

@Injectable({
  providedIn: 'root'
})
export class PortfolioService {

  private readonly apiUrl = `${environment.apiURL}/api/portfolios`;

  constructor(private httpClient: HttpClient) {
  }

  listByUser(userId: number): Observable<Portfolio[]> {
    return this.httpClient.get<Portfolio[]>(`${this.apiUrl}?userId=${userId}`);
  }

  createPortfolio(userId: number, portfolioName: string): Observable<Portfolio> {
    return this.httpClient.post<Portfolio>(this.apiUrl, { userId, portfolioName });
  }

  getPositions(portfolioId: number): Observable<PortfolioPosition[]> {
    return this.httpClient.get<PortfolioPosition[]>(`${this.apiUrl}/${portfolioId}/positions`);
  }
}
