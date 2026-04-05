import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { Company } from '../models/company-model';

export type CompanyStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED' | 'DEACTIVATED' | 'BANNED';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  private apiUrl:string;
  private apiPaths: {[apiName:string]: string};

  constructor(private httpClient:HttpClient) {
    this.apiUrl = environment.apiURL+"/api/companies";
    this.apiPaths = {
      "getAllCompanies":this.apiUrl,
      "getCompanyById":this.apiUrl,
      "getCompanyByName":this.apiUrl+"/search",
      "updateCompany":this.apiUrl,
      "deleteCompany":this.apiUrl,
      "addCompany":this.apiUrl,
      "getPendingApprovalCompanies":this.apiUrl+"/pending-approval"
    }
  }

  public getAllCompanies():Observable<Company[]>{
    return this.httpClient.get<{data: Company[]}>(this.apiPaths.getAllCompanies).pipe(map(response => response.data));
  }

  public getCompanyByName(pattern:string):Observable<Company[]>{
    return this.httpClient.get<{data: Company[]}>(this.apiPaths.getCompanyByName+"/"+pattern).pipe(map(response => response.data));
  }

  public addCompany(company:Company):Observable<Company>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.post<{data: Company}>(this.apiPaths.addCompany, company, httpOptions).pipe(map(response => response.data));
  }

  public updateCompany(id:number, company:Company):Observable<Company>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.put<{data: Company}>(this.apiPaths.updateCompany+"/"+id, company, httpOptions).pipe(map(response => response.data));
  }

  public deleteCompany(id:number):Observable<Company>{
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type':  'application/json'
      })
    };
    return this.httpClient.delete<{data: Company}>(this.apiPaths.deleteCompany+"/"+id, httpOptions).pipe(map(response => response.data));
  }

  public getCompanyById(id:number):Observable<Company>{
    return this.httpClient.get<{data: Company}>(this.apiPaths.getCompanyById+"/"+id).pipe(map(response => response.data));
  }

  public getCompaniesByExchange(exchangeId:number):Observable<Company[]>{
    return this.httpClient.get<{data: Company[]}>(this.apiUrl+"/by-exchange/"+exchangeId).pipe(map(response => response.data));
  }

  public getPendingApprovalCompanies():Observable<Company[]>{
    return this.httpClient.get<{data: Company[]}>(this.apiPaths.getPendingApprovalCompanies).pipe(map(response => response.data));
  }

  public updateCompanyStatus(id:number, action:'approve'|'reject'|'suspend'|'deactivate'|'ban'):Observable<Company>{
    return this.httpClient.put<{data: Company}>(`${this.apiUrl}/${id}/${action}`, {}).pipe(map(response => response.data));
  }

}
