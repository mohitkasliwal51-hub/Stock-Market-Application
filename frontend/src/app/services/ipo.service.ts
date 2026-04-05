import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Ipo } from '../models/ipo-model';

@Injectable({
  providedIn: 'root'
})
export class IpoService {

  private apiUrl:string;
  private apiPaths: {[apiName:string]: string};

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL+"/api/ipos";
    this.apiPaths = {
      "getAllIpos":this.apiUrl,
      "getIpoByCompany":this.apiUrl+"/by-company",
      "updateIpo":this.apiUrl,
      "addIpo":this.apiUrl
    }
  }

  public getAllIpos():Observable<Ipo[]>{
    return this.httpClient.get<{data: Ipo[]}>(this.apiPaths.getAllIpos).pipe(map(response => response.data));
  }

  public getIpoByCompany(id:number):Observable<Ipo>{
    return this.httpClient.get<{data: Ipo}>(this.apiPaths.getIpoByCompany+"/"+id).pipe(map(response => response.data));
  }

  public addIpo(ipo:Ipo):Observable<Ipo>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.post<{data: Ipo}>(this.apiPaths.addIpo, ipo, httpOptions).pipe(map(response => response.data));
  }

  public updateIpo(id:number, ipo:Ipo):Observable<Ipo>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.put<{data: Ipo}>(this.apiPaths.updateIpo+"/"+id, ipo, httpOptions).pipe(map(response => response.data));
  }

  public deleteIpo(id:number):Observable<Ipo>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.delete<{data: Ipo}>(this.apiPaths.getAllIpos+"/"+id, httpOptions).pipe(map(response => response.data));
  }

}
