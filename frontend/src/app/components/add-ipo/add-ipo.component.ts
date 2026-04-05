import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from "@angular/router"
import { Company } from 'src/app/models/company-model';
import { Exchange } from 'src/app/models/exchange-model';
import { Ipo } from 'src/app/models/ipo-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { IpoService } from 'src/app/services/ipo.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
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
  public errorMessage:string = '';
  public successMessage:string = '';
  public isLoading:boolean = false;

  constructor(private authService:AuthService, private companyService:CompanyService, private exchangeService:ExchangeService, private ipoService:IpoService, private router: Router, private activatedRoute:ActivatedRoute, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService) {
    this.state="";
    this.companyTitle="Please choose a company";
    this.exchangeTitle="Please choose a stock exchange";
    this.companies=[];
    this.exchanges=[];
    this.companyId=Number(this.activatedRoute.snapshot.params["id"] || 0);
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
    if (this.companyId) {
      this.loadIpoData();
    }
    this.companyService.getAllCompanies().subscribe(companies => {
      this.companies = companies;
      this.syncSelectedLabels();
      this.cdr.detectChanges();
    });
    this.exchangeService.getAllExchanges().subscribe(exchanges => {
      this.exchanges = exchanges;
      this.syncSelectedLabels();
      this.cdr.detectChanges();
    });
  }

  private loadIpoData(): void {
    this.ipoService.getIpoByCompany(this.companyId).subscribe(ipo => {
      this.ipo = {
        id: Number(ipo.id || 0),
        pricePerShare: Number(ipo.pricePerShare || 0),
        totalShares: Number(ipo.totalShares || 0),
        dateTime: (ipo.dateTime || '').toString(),
        remarks: ipo.remarks || '',
        companyId: Number(ipo.companyId || this.companyId),
        stockExchangeId: Number(ipo.stockExchangeId || 0)
      };
      this.syncSelectedLabels();
      this.ipo.dateTime = this.formatForInput(this.ipo.dateTime);
      this.cdr.detectChanges();
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
    this.errorMessage = '';
    this.successMessage = '';

    if (this.ipo.companyId === 0) {
      this.errorMessage = 'Please choose a company';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (this.ipo.stockExchangeId === 0) {
      this.errorMessage = 'Please choose a stock exchange';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (this.ipo.pricePerShare <= 0 || this.ipo.totalShares <= 0) {
      this.errorMessage = 'Price per share and total shares must be greater than zero';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (!this.ipo.dateTime?.trim()) {
      this.errorMessage = 'Opening date and time is required';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    console.log(this.ipo);
    const payload:Ipo = {
      ...this.ipo,
      dateTime: this.toApiDateTime(this.ipo.dateTime)
    };

    this.isLoading = true;
    this.liveAnnouncer.announceStatus(this.companyId ? 'Updating IPO details.' : 'Submitting IPO details.');
    const request$ = this.companyId
      ? this.ipoService.updateIpo(this.companyId, payload)
      : this.ipoService.addIpo(payload);

    request$.subscribe({
      next: (ipo) => {
        console.log(ipo);
        this.successMessage = this.companyId ? 'IPO updated successfully!' : 'IPO added successfully!';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/ipo']), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Failed to save IPO';
        this.liveAnnouncer.announceError(this.errorMessage);
        this.cdr.detectChanges();
      }
    });
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

  private formatForInput(value:string):string {
    if (!value) {
      return '';
    }
    return value.length >= 16 ? value.substring(0, 16) : value;
  }

  private toApiDateTime(value:string):string {
    if (!value) {
      return value;
    }
    if (value.includes('+') || value.endsWith('Z')) {
      return value;
    }
    return `${value}:00.000+05:30`;
  }

}
