import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Company } from 'src/app/models/company-model';
import { Exchange } from 'src/app/models/exchange-model';
import { Stock } from 'src/app/models/stock-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { StockService } from 'src/app/services/stock.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-stock',
  templateUrl: './stock.component.html',
  styleUrls: ['./stock.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class StockComponent implements OnInit {

  public state:string;
  public stock:Stock;
  public companies:Company[];
  public exchanges:Exchange[];
  public companyTitle:string;
  public exchangeTitle:string;
  public errorMessage:string = '';
  public successMessage:string = '';
  public isLoading:boolean = false;

  constructor(private authService:AuthService, private companyService:CompanyService, private exchangeService:ExchangeService, private stockService:StockService, private router: Router, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService) {
    this.state="";
    this.companyTitle="Please choose a company";
    this.exchangeTitle="Please choose a stock exchange";
    this.stock={
      "id": 0,
      "stockCode": "",
      "companyId": 0,
      "stockExchangeId": 0
    }
    this.companies=[];
    this.exchanges=[];
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
  }

  onCompanyClick(company:Company){
    this.companyTitle = company.companyName || company.name || '';
    this.stock.companyId = company.id;
  }

  onExchangeClick(exchange:Exchange){
    this.exchangeTitle = exchange.name;
    this.stock.stockExchangeId = exchange.id;
  }

  addStock(){
    this.errorMessage = '';
    this.successMessage = '';
    
    if(this.stock.companyId === 0){
      this.errorMessage = "Please choose a company";
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }
    if(this.stock.stockExchangeId === 0){
      this.errorMessage = "Please choose a stock exchange";
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }
    if(!this.stock.stockCode || this.stock.stockCode.trim() === ''){
      this.errorMessage = "Stock code is required";
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }
    
    this.isLoading = true;
    this.liveAnnouncer.announceStatus('Submitting stock details.');
    this.stockService.addStock(this.stock).subscribe({
      next: (addedStock: Stock) => {
        console.log(addedStock);
        this.successMessage = "Stock added successfully!";
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/exchange']), 1500);
      },
      error: (err) => {
        this.isLoading = false;
          this.errorMessage = err?.message || 'Failed to add stock. Please check your entries and try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
        this.cdr.detectChanges();
      }
    })
  }

  onReset(){
    this.stock={
      "id": 0,
      "stockCode": "",
      "companyId": 0,
      "stockExchangeId": 0
    }
    this.companyTitle='Please choose a company';
    this.exchangeTitle='Please choose a stock exchange';
  }

}
