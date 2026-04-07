import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Company } from 'src/app/models/company-model';
import { Sector } from 'src/app/models/sector-model';
import { AppDialogService } from 'src/app/services/app-dialog.service';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { SectorService } from 'src/app/services/sector.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-company',
  templateUrl: './company.component.html',
  styleUrls: ['./company.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent]
})
export class CompanyComponent implements OnInit {

  public state:string;
  public companies:Company[];
  public sectors:Sector[];
  public pattern:string;
  public errorMessage:string;
  public successMessage:string;
  public statusMessage:string;

  constructor(private authService:AuthService, private companyService:CompanyService, private sectorService:SectorService, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService, private appDialog: AppDialogService) {
    this.state="";
    this.companies=[];
    this.sectors=[];
    this.pattern="";
    this.errorMessage = '';
    this.successMessage = '';
    this.statusMessage = '';
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.getAllCompanies();
    this.sectorService.getAllSectors().subscribe(allSectors => {
      this.sectors = allSectors;
      this.cdr.detectChanges();
    });
  }

  public getAllCompanies(){
    this.companyService.getAllCompanies().subscribe( allCompanies => {
      this.companies = allCompanies;
      this.cdr.detectChanges();
    });
  }

  public getCompanyByPattern(){
    this.errorMessage = '';
    this.successMessage = '';
    if(this.pattern != ""){
      this.statusMessage = 'Searching companies by name.';
      this.liveAnnouncer.announceStatus(this.statusMessage);
      this.companyService.getCompanyByName(this.pattern).subscribe( foundCompanies => {
        this.companies = foundCompanies;
        this.successMessage = `${foundCompanies.length} compan${foundCompanies.length === 1 ? 'y' : 'ies'} found.`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.cdr.detectChanges();
      });
    } else {
      this.getAllCompanies();
    }
  }

  public async deleteCompany(company:Company){
    const companyName = this.getCompanyName(company);
    const confirmed = await this.appDialog.confirm(`Do you want to deactivate company ${companyName} with ID ${company.id}?`, {
      title: 'Deactivate Company',
      confirmLabel: 'Deactivate'
    });
    if (!confirmed) {
      return;
    }

    this.statusMessage = `Deactivating company ${companyName} (ID ${company.id}).`;
    this.liveAnnouncer.announceStatus(this.statusMessage);
    this.companyService.deleteCompany(company.id).subscribe({
      next: () =>{
        this.successMessage = `Company ${companyName} (ID ${company.id}) deactivated successfully.`;
        this.errorMessage = '';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.getAllCompanies();
        this.cdr.detectChanges();
      },
      error: (err) => {
          this.errorMessage = err?.message || `Failed to deactivate company. Please try again.`;
        this.successMessage = '';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  public getCompanyName(company:Company):string{
    return company.companyName || company.name || '';
  }

  public getCompanyBrief(company:Company):string{
    return company.briefWriteup || company.brief || '';
  }

  public getCompanyStatus(company:Company):string{
    return company.status || 'PENDING';
  }

  public isActionAllowed(company:Company, action:'approve'|'reject'|'suspend'|'deactivate'|'ban'):boolean{
    const status = this.getCompanyStatus(company);
    if (action === 'approve') {
      return status === 'PENDING' || status === 'SUSPENDED' || status === 'DEACTIVATED';
    }
    if (action === 'reject') {
      return status === 'PENDING';
    }
    if (action === 'suspend') {
      return status === 'APPROVED';
    }
    if (action === 'deactivate') {
      return status === 'APPROVED' || status === 'SUSPENDED';
    }
    if (action === 'ban') {
      return status !== 'BANNED';
    }
    return false;
  }

  public async updateCompanyStatus(company:Company, action:'approve'|'reject'|'suspend'|'deactivate'|'ban'){
    const actionLabels = {
      'approve': 'approve',
      'reject': 'reject',
      'suspend': 'suspend',
      'deactivate': 'deactivate',
      'ban': 'ban'
    };
    const companyName = this.getCompanyName(company);
    const confirmed = await this.appDialog.confirm(`Do you want to ${actionLabels[action]} company ${companyName} with ID ${company.id}?`, {
      title: 'Update Company Status',
      confirmLabel: 'Proceed'
    });
    if (!confirmed) {
      return;
    }

    this.statusMessage = `Updating company ${companyName} (ID ${company.id}) status to ${action}.`;
    this.liveAnnouncer.announceStatus(this.statusMessage);
    this.companyService.updateCompanyStatus(company.id, action).subscribe({
      next: () => {
        this.successMessage = `Company ${companyName} (ID ${company.id}) ${action}d successfully.`;
        this.errorMessage = '';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.getAllCompanies();
        this.cdr.detectChanges();
      },
      error: (err) => {
          this.errorMessage = err?.message || `Failed to update company. Please try again.`;
        this.successMessage = '';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  public getSectorName(company:Company):string{
    if (company.sector?.name) {
      return company.sector.name;
    }
    const sector = this.sectors.find(s => s.id === company.sectorId);
    return sector?.name || 'N/A';
  }

}
