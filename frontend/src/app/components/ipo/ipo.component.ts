import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Company } from 'src/app/models/company-model';
import { Exchange } from 'src/app/models/exchange-model';
import { Ipo } from 'src/app/models/ipo-model';
import { AppDialogService } from 'src/app/services/app-dialog.service';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { IpoService } from 'src/app/services/ipo.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
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
  public errorMessage:string;
  public successMessage:string;

  constructor(private authService:AuthService, private ipoService:IpoService, private companyService:CompanyService, private exchangeService:ExchangeService, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService, private appDialog: AppDialogService) {
    this.state="";
    this.ipos=[];
    this.allIpos=[];
    this.companies=[];
    this.exchanges=[];
    this.dropDownTitle = "Please select company";
    this.currentCompanyId = 0;
    this.errorMessage = '';
    this.successMessage = '';
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
    this.errorMessage = '';
    this.successMessage = '';
    if(this.dropDownTitle==="Please select company"){
      this.errorMessage = 'Please select a Company';
      this.liveAnnouncer.announceError(this.errorMessage);
    } else{
      this.liveAnnouncer.announceStatus('Loading IPO data for selected company.');
      this.ipoService.getIpoByCompany(this.currentCompanyId).subscribe( ipo => {
        this.ipos=[];
        this.ipos.push(ipo);
        this.successMessage = 'IPO data loaded successfully';
        this.liveAnnouncer.announceSuccess(this.successMessage);
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

  public async deleteIpo(ipo:Ipo):Promise<void>{
    const companyName = this.getCompanyName(ipo.companyId);
    const confirmed = await this.appDialog.confirm(`Do you want to delete IPO ${ipo.id} for company ${companyName} (ID ${ipo.companyId})?`, {
      title: 'Delete IPO',
      confirmLabel: 'Delete'
    });
    if (!confirmed) {
      return;
    }

    this.liveAnnouncer.announceStatus(`Deleting IPO ${ipo.id} for company ${companyName} (ID ${ipo.companyId}).`);
    this.ipoService.deleteIpo(ipo.id).subscribe({
      next: () => {
        this.successMessage = `IPO ${ipo.id} for company ${companyName} (ID ${ipo.companyId}) deleted successfully.`;
        this.errorMessage = '';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.getAllIpos();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.error?.error?.message || `Failed to delete IPO ${ipo.id} for company ${companyName} (ID ${ipo.companyId}).`;
        this.successMessage = '';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

}
