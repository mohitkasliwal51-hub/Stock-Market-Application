import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Company } from 'src/app/models/company-model';
import { Exchange } from 'src/app/models/exchange-model';
import { Ipo } from 'src/app/models/ipo-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { IpoService } from 'src/app/services/ipo.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-ipo',
  templateUrl: './ipo.component.html',
  styleUrls: ['./ipo.component.css'],
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent]
})
export class IpoComponent implements OnInit {

  public state:string;
  public ipos:Ipo[];
  public allIpos:Ipo[];
  public companies:Company[];
  public exchanges:Exchange[];
  public dropDownTitle:string;
  public currentCompanyId:number;

  constructor(private authService:AuthService, private ipoService:IpoService, private companyService:CompanyService, private exchangeService:ExchangeService, private cdr: ChangeDetectorRef) {
    this.state="";
    this.ipos=[];
    this.allIpos=[];
    this.companies=[];
    this.exchanges=[];
    this.dropDownTitle = "Please select company";
    this.currentCompanyId = 0;
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.companyService.getAllCompanies().subscribe(companies => {
      this.companies = companies;
      this.cdr.detectChanges();
    });
    this.exchangeService.getAllExchanges().subscribe(exchanges => {
      this.exchanges = exchanges;
      this.cdr.detectChanges();
    });
    this.getAllIpos();
  }

  public getAllIpos(){
    this.ipoService.getAllIpos().subscribe( allIpos => {
      this.ipos = allIpos;
      this.allIpos = allIpos;
      this.cdr.detectChanges();
    });
  }

  onCompanyClick(companyId:number){
    this.currentCompanyId = companyId;
    const company = this.companies.find(c => c.id === companyId);
    this.dropDownTitle = company ? (company.companyName || company.name || '') : 'Please select company';
  }

  onLoadComapnies(){
    if(this.dropDownTitle==="Please select company"){
      alert("Please select a Company");
    } else{
      this.ipoService.getIpoByCompany(this.currentCompanyId).subscribe( ipo => {
        this.ipos=[];
        this.ipos.push(ipo);
        this.cdr.detectChanges();
      })
    }
  }

  public clearFilter(){
    this.dropDownTitle = "Please select company";
    this.currentCompanyId = 0;
    this.getAllIpos();
  }

  public getCompanyName(companyId:number):string{
    const company = this.companies.find(c => c.id === companyId);
    return company ? (company.companyName || company.name || `Company #${companyId}`) : `Company #${companyId}`;
  }

  public getExchangeName(exchangeId:number):string{
    const exchange = this.exchanges.find(e => e.id === exchangeId);
    return exchange?.name || `Exchange #${exchangeId}`;
  }

  public getCompanyId(ipo:Ipo):number{
    return ipo.companyId;
  }

  public getStockExchangeId(ipo:Ipo):number{
    return ipo.stockExchangeId;
  }

  public deleteIpo(id:number):void{
    this.ipoService.deleteIpo(id).subscribe(() => {
      console.log('IPO deleted');
      this.getAllIpos();
      this.cdr.detectChanges();
    });
  }

}
