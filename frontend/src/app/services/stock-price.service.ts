import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { StockPrice } from '../models/stock-price-model';

@Injectable({
  providedIn: 'root'
})
export class StockPriceService {

  private apiUrl:string;
  private apiPaths: {[apiName:string]: string};

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL+"/api";
    this.apiPaths = {
      "getAllStockPrices":this.apiUrl+"/stock-prices",
      "getStockPrice":this.apiUrl+"/stock-prices/by-company",
      "addStockPrices":this.apiUrl+"/stock-prices"
    }
  }

  public getAllStockPrices():Observable<StockPrice[]>{
    return this.httpClient.get<{data: StockPrice[]}>(this.apiPaths.getAllStockPrices).pipe(map(response => response.data));
  }


  public getStockPricesByCompany(companyId:number, exchangeId:number, from:string, to:string):Observable<StockPrice[]>{
    return this.httpClient.get<{data: StockPrice[]}>(this.apiPaths.getStockPrice+`/${companyId}/${exchangeId}/${encodeURIComponent(from)}/${encodeURIComponent(to)}`).pipe(map(response => response.data));
  }

  public addStockPrices(stockPrices:StockPrice[]):Observable<StockPrice[]>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.post<{data: StockPrice[]}>(this.apiPaths.addStockPrices, stockPrices, httpOptions).pipe(map(response => response.data));
  }
}
