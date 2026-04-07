import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface Wallet {
  walletId: number;
  userId: number;
  balance: number;
  currency: string;
  createdAt: string;
  lastUpdated: string;
}

export interface WalletTransaction {
  id: number;
  transactionType: string;
  amount: number;
  status: string;
  description: string;
  referenceId: string;
  transactionDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class WalletService {

  private readonly apiUrl = `${environment.apiURL}/api/wallets`;

  constructor(private httpClient: HttpClient) {
  }

  createWallet(userId: number, initialBalance: number, currency: string = 'INR'): Observable<Wallet> {
    return this.httpClient.post<Wallet>(this.apiUrl, { userId, initialBalance, currency });
  }

  getWallet(userId: number): Observable<Wallet> {
    return this.httpClient.get<Wallet>(`${this.apiUrl}/${userId}`);
  }

  deposit(userId: number, amount: number, referenceId: string, description: string): Observable<Wallet> {
    return this.httpClient.post<Wallet>(`${this.apiUrl}/${userId}/deposit`, {
      amount,
      referenceId,
      description
    });
  }

  getTransactions(userId: number): Observable<WalletTransaction[]> {
    return this.httpClient.get<WalletTransaction[]>(`${this.apiUrl}/${userId}/transactions`);
  }
}
