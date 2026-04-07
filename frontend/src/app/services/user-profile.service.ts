import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface UserProfile {
  id: number;
  username: string;
  email: string;
  role: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {

  private readonly apiUrl = `${environment.apiURL}/api/auth/me`;
  private readonly storageKey = 'stock.profile';

  constructor(private httpClient: HttpClient) {
  }

  getProfile(): Observable<UserProfile> {
    return this.httpClient.get<ApiResponse<UserProfile>>(this.apiUrl).pipe(
      map(response => response.data),
      tap(profile => localStorage.setItem(this.storageKey, JSON.stringify(profile)))
    );
  }

  getCachedProfile(): UserProfile | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as UserProfile;
    } catch (_err) {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }

  clearCache(): void {
    localStorage.removeItem(this.storageKey);
  }
}
