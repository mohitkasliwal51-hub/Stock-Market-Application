import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Stock } from '../models/stock-model';

@Injectable({
  providedIn: 'root'
})
export class StockService {

  private apiUrl:string;
  private apiPaths: {[apiName:string]: string};

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL+"/api/stocks";
    this.apiPaths = {
      "getAllStocks":this.apiUrl,
      "addStock":this.apiUrl
    }
  }

  public getAllStocks():Observable<Stock[]>{
    return this.httpClient.get<{data: Stock[]}>(this.apiPaths.getAllStocks).pipe(map(response => response.data));
  }

  public addStock(stock:Stock):Observable<Stock>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.post<{data: Stock}>(this.apiPaths.addStock, stock, httpOptions).pipe(map(response => response.data));
  }

}