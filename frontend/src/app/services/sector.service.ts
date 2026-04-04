import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Company } from '../models/company-model';
import { Sector } from '../models/sector-model';

@Injectable({
  providedIn: 'root'
})
export class SectorService {
  private apiUrl:string;
  private apiPaths: {[apiName:string]: string};

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL+"/api/sectors";
    this.apiPaths = {
      "getAllSectors":this.apiUrl,
      "getSectorById":this.apiUrl,
      "addSector":this.apiUrl,
      "updateSector":this.apiUrl,
      "deleteSector":this.apiUrl,
      "getCompaniesBySector":this.apiUrl
    }
  }

  public getAllSectors():Observable<Sector[]>{
    return this.httpClient.get<{data: Sector[]}>(this.apiPaths.getAllSectors).pipe(map(response => response.data));
  }

  public getSectorById(id:number):Observable<Sector>{
    return this.httpClient.get<{data: Sector}>(this.apiPaths.getSectorById+"/"+id).pipe(map(response => response.data));
  }

  public addSector(sector:Sector):Observable<Sector>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.post<{data: Sector}>(this.apiPaths.addSector, sector, httpOptions).pipe(map(response => response.data));
  }

  public updateSector(id:number, sector:Sector):Observable<Sector>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.put<{data: Sector}>(this.apiPaths.updateSector+"/"+id, sector, httpOptions).pipe(map(response => response.data));
  }

  public deleteSector(id:number):Observable<Sector>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.delete<{data: Sector}>(this.apiPaths.deleteSector+"/"+id, httpOptions).pipe(map(response => response.data));
  }

  public getCompaniesBySector(id:number):Observable<Company[]>{
    return this.httpClient.get<{data: Company[]}>(this.apiPaths.getCompaniesBySector+"/"+id+"/companies").pipe(map(response => response.data));
  }

  public getCompanyBySector(id:number):Observable<Company[]>{
    return this.getCompaniesBySector(id);
  }

}
