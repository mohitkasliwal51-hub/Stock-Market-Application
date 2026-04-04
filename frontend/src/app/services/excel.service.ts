import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { ExcelData } from '../models/excel-data';

@Injectable({
  providedIn: 'root'
})
export class ExcelService {

  private apiUrl:string;

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL + '/api/excel';
  }

  public health():Observable<null>{
    return this.httpClient.get<{data: null}>(this.apiUrl + '/health').pipe(map(response => response.data));
  }

  public uploadData(excelData:ExcelData[]):Observable<ExcelData[]>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    };
    return this.httpClient.post<{data: ExcelData[]}>(this.apiUrl + '/uploadData', excelData, httpOptions).pipe(map(response => response.data));
  }

}