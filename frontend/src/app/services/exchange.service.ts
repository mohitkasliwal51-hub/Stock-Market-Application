import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Company } from '../models/company-model';
import { Exchange } from '../models/exchange-model';

@Injectable({
  providedIn: 'root'
})
export class ExchangeService {

  private apiUrl:string;
  private apiPaths: {[apiName:string]: string};

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL+"/api/stockExchanges";
    this.apiPaths = {
      "getAllExchanges":this.apiUrl,
      "getExchangeById":this.apiUrl,
      "addExchange":this.apiUrl,
      "updateExchange":this.apiUrl,
      "deleteExchange":this.apiUrl,
      "getCompaniesByExchange":this.apiUrl
    }
  }

  public getAllExchanges():Observable<Exchange[]>{
    return this.httpClient.get<{data: Exchange[]}>(this.apiPaths.getAllExchanges).pipe(map(response => response.data));
  }

  public getExchangeById(id:number):Observable<Exchange>{
    return this.httpClient.get<{data: Exchange}>(this.apiPaths.getExchangeById+"/"+id).pipe(map(response => response.data));
  }

  public addExchange(exchange:Exchange):Observable<Exchange>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.post<{data: Exchange}>(this.apiPaths.addExchange, exchange, httpOptions).pipe(map(response => response.data));
  }

  public updateExchange(id:number, exchange:Exchange):Observable<Exchange>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.put<{data: Exchange}>(this.apiPaths.updateExchange+"/"+id, exchange, httpOptions).pipe(map(response => response.data));
  }

  public deleteExchange(id:number):Observable<Exchange>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.delete<{data: Exchange}>(this.apiPaths.deleteExchange+"/"+id, httpOptions).pipe(map(response => response.data));
  }

  public getCompaniesByExchange(id:number):Observable<Company[]>{
    return this.httpClient.get<{data: Company[]}>(this.apiPaths.getCompaniesByExchange+"/"+id+"/companies").pipe(map(response => response.data));
  }

}
