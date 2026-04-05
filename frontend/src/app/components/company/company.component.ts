import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Company } from 'src/app/models/company-model';
import { Sector } from 'src/app/models/sector-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
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

  constructor(private authService:AuthService, private companyService:CompanyService, private sectorService:SectorService, private cdr: ChangeDetectorRef) {
    this.state="";
    this.companies=[];
    this.sectors=[];
    this.pattern="";
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
    if(this.pattern != ""){
      this.companyService.getCompanyByName(this.pattern).subscribe( foundCompanies => {
        this.companies = foundCompanies;
        this.cdr.detectChanges();
      });
    } else {
      this.getAllCompanies();
    }
  }

  public deleteCompany(id:number){
    this.companyService.deleteCompany(id).subscribe( company =>{
      console.log(company);
      this.getAllCompanies();
      this.cdr.detectChanges();
    })
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

  public updateCompanyStatus(id:number, action:'approve'|'reject'|'suspend'|'deactivate'|'ban'){
    this.companyService.updateCompanyStatus(id, action).subscribe(updatedCompany => {
      console.log(updatedCompany);
      this.getAllCompanies();
      this.cdr.detectChanges();
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
