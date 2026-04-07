import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface MarketStatus {
  marketOpen: boolean;
  timezone: string;
  tradingDate: string;
  currentTime: string;
  opensAt: string;
  closesAt: string;
  reason: string;
  nextOpenAt: string;
}

export interface LivePrice {
  stockId: number;
  stockCode: string;
  exchangeId: number;
  currentPrice: number;
  openingPrice: number;
  dayChange: number;
  lastUpdated: string;
}

@Injectable({
  providedIn: 'root'
})
export class MarketLiveService {

  private readonly apiUrl = `${environment.apiURL}/api/market`;

  constructor(private httpClient: HttpClient) {
  }

  getStatus(): Observable<MarketStatus> {
    return this.httpClient.get<MarketStatus>(`${this.apiUrl}/status`);
  }

  getLivePrices(): Observable<LivePrice[]> {
    return this.httpClient.get<LivePrice[]>(`${this.apiUrl}/prices/live`);
  }
}
