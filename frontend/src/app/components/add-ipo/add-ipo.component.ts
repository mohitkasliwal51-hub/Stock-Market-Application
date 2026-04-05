import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from "@angular/router"
import { Company } from 'src/app/models/company-model';
import { Exchange } from 'src/app/models/exchange-model';
import { Ipo } from 'src/app/models/ipo-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { IpoService } from 'src/app/services/ipo.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-add-ipo',
  templateUrl: './add-ipo.component.html',
  styleUrls: ['./add-ipo.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class AddIpoComponent implements OnInit {

  public state:string
  public ipo:Ipo;
  public companies:Company[];
  public exchanges:Exchange[];
  public companyTitle:string;
  public exchangeTitle:string;
  public companyId:number;

  constructor(private authService:AuthService, private companyService:CompanyService, private exchangeService:ExchangeService, private ipoService:IpoService, private router: Router, private activatedRoute:ActivatedRoute) {
    this.state="";
    this.companyTitle="Please choose a company";
    this.exchangeTitle="Please choose a stock exchange";
    this.companies=[];
    this.exchanges=[];
    this.companyId=this.activatedRoute.snapshot.params["id"];
    this.ipo={
      "id": 0,
      "pricePerShare": 0,
      "totalShares": 0,
      "dateTime": "",
      "remarks": "",
      "companyId": 0,
      "stockExchangeId": 0
    }
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.companyService.getAllCompanies().subscribe(companies => {
      this.companies = companies;
      if (this.companyId) {
        this.loadIpoData();
      } else {
        this.syncSelectedLabels();
      }
    });
    this.exchangeService.getAllExchanges().subscribe(exchanges => {
      this.exchanges = exchanges;
      this.syncSelectedLabels();
    });
  }

  private loadIpoData(): void {
    this.ipoService.getIpoByCompany(this.companyId).subscribe(ipo => {
      this.ipo = ipo;
      this.syncSelectedLabels();
      this.ipo.dateTime = this.ipo.dateTime.substr(0, 16);
    });
  }

  onCompanyClick(company:Company){
    this.companyTitle = company.companyName || company.name || '';
    this.ipo.companyId = company.id;
  }

  onExchangeClick(exchange:Exchange){
    this.exchangeTitle = exchange.name;
    this.ipo.stockExchangeId = exchange.id;
  }

  addStock(){
    console.log(this.ipo);
    var date: string = this.ipo.dateTime+":00.000+05:30";
    this.ipo.dateTime = date;
    if (this.companyId) {
      this.ipoService.updateIpo(this.companyId, this.ipo).subscribe( updatedIpo => {
        console.log(updatedIpo);
      })
    } else {
      this.ipoService.addIpo(this.ipo).subscribe( addedIpo => {
        console.log(addedIpo);
      })
    }
    this.router.navigate(['/ipo']);
  }

  onReset(){
    this.ipo={
      "id": 0,
      "pricePerShare": 0,
      "totalShares": 0,
      "dateTime": "",
      "remarks": "",
      "companyId": 0,
      "stockExchangeId": 0
    }
    this.companyTitle='Please choose a company';
    this.exchangeTitle='Please choose a stock exchange';
  }

  private syncSelectedLabels(){
    const company = this.companies.find(c => c.id === this.ipo.companyId);
    if (company) {
      this.companyTitle = company.companyName || company.name || '';
    }
    const exchange = this.exchanges.find(e => e.id === this.ipo.stockExchangeId);
    if (exchange) {
      this.exchangeTitle = exchange.name;
    }
  }

}
