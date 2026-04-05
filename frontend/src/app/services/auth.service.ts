import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly authApiUrl = `${environment.apiURL}/api/auth`;
  private readonly tokenStorageKey = 'stock.jwt';
  private readonly userStorageKey = 'stock.user';

  constructor(private httpClient:HttpClient) {
  }

  login(username:string,password:string):Observable<boolean>{
    const payload = { username, password };
    return this.httpClient.post<ApiResponse<AuthPayload>>(`${this.authApiUrl}/login`, payload).pipe(
      tap(response => this.persistAuth(response.data)),
      map(() => true)
    );
  }

  register(username:string,password:string,email:string):Observable<boolean>{
    const payload = { username, password, email, role: 'INVESTOR' };
    return this.httpClient.post<ApiResponse<AuthPayload>>(`${this.authApiUrl}/register`, payload).pipe(
      tap(response => this.persistAuth(response.data)),
      map(() => true)
    );
  }

  logout(){
    localStorage.removeItem(this.tokenStorageKey);
    localStorage.removeItem(this.userStorageKey);
  }

  authenticate(username:string,password:string){
    if (username === 'unauth' && password === 'unauth') {
      this.logout();
    }
  }

  getCurrentUserRole():string{
    const user = this.getCurrentUser();
    if (!user) {
      return 'unauthorized';
    }

    if (user.role === 'ADMIN') {
      return 'admin';
    }

    return 'user';
  }

  isAuthenticated():boolean{
    return !!this.getAccessToken() && this.getCurrentUserRole() !== 'unauthorized';
  }

  isAdmin():boolean{
    return this.getCurrentUserRole() === 'admin';
  }

  getAccessToken():string{
    return localStorage.getItem(this.tokenStorageKey) || '';
  }

  getAuthHeaders():HttpHeaders{
    const token = this.getAccessToken();
    return new HttpHeaders({ Authorization: `Bearer ${token}` });
  }

  private persistAuth(payload:AuthPayload){
    localStorage.setItem(this.tokenStorageKey, payload.token);
    localStorage.setItem(this.userStorageKey, JSON.stringify({ username: payload.username, role: payload.role }));
  }

  private getCurrentUser():StoredUser | null {
    const raw = localStorage.getItem(this.userStorageKey);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as StoredUser;
    } catch (_err) {
      this.logout();
      return null;
    }
  }

}

interface ApiResponse<T>{
  success:boolean;
  message:string;
  data:T;
  errorCode?:string;
}

interface AuthPayload{
  token:string;
  username:string;
  role:string;
}

interface StoredUser{
  username:string;
  role:string;
}
