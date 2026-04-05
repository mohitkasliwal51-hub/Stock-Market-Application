import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from "@angular/router"
import { Company } from 'src/app/models/company-model';
import { Sector } from 'src/app/models/sector-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { SectorService } from 'src/app/services/sector.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-add-company',
  templateUrl: './add-company.component.html',
  styleUrls: ['./add-company.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class AddCompanyComponent implements OnInit {

  public state:string;
  public company:Company;
  public sectors:Sector[];
  public dropDownTitle:string;
  public companyId:number;
  public errorMessage:string = '';
  public successMessage:string = '';
  public isLoading:boolean = false;

  constructor(private authService:AuthService, private sectorService:SectorService, private companyService:CompanyService, private router: Router, private activatedRoute:ActivatedRoute, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService) {
    this.state="";
    this.sectors=[];
    this.dropDownTitle="Please Choose a Sector"
    this.companyId = Number(this.activatedRoute.snapshot.params["id"] || 0);
    this.company = {
      "id":0,
      "companyName": "",
      "turnover": 0,
      "ceo": "",
      "briefWriteup": "",
      "boardOfDirectors": "",
      "sectorId": 0
    }
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    if (this.companyId) {
      this.loadCompanyData();
    }
    this.sectorService.getAllSectors().subscribe( sectors => {
      this.sectors = sectors;
      this.syncSelectedSectorTitle();
      this.cdr.detectChanges();
    })
  }

  private loadCompanyData(): void {
    this.companyService.getCompanyById(this.companyId).subscribe(companyFound => {
      this.company = {
        id: companyFound.id || 0,
        companyName: companyFound.companyName || companyFound.name || '',
        turnover: Number(companyFound.turnover || 0),
        ceo: companyFound.ceo || '',
        briefWriteup: companyFound.briefWriteup || companyFound.brief || '',
        boardOfDirectors: companyFound.boardOfDirectors || companyFound.bod || '',
        sectorId: Number(companyFound.sectorId || companyFound.sector?.id || 0),
        status: companyFound.status,
        name: companyFound.name,
        brief: companyFound.brief,
        bod: companyFound.bod,
        sector: companyFound.sector
      };
      this.syncSelectedSectorTitle();
      this.cdr.detectChanges();
    });
  }

  addCompany(){
    this.errorMessage = '';
    this.successMessage = '';

    if(this.company.sectorId === 0){
      this.errorMessage = 'Please choose a sector';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (!this.company.companyName?.trim()) {
      this.errorMessage = 'Company name is required';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (this.company.turnover <= 0) {
      this.errorMessage = 'Turnover must be greater than zero';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (!this.company.ceo?.trim() || !this.company.briefWriteup?.trim() || !this.company.boardOfDirectors?.trim()) {
      this.errorMessage = 'Please fill all required fields';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.isLoading = true;
    this.liveAnnouncer.announceStatus(this.companyId ? 'Updating company details.' : 'Submitting company details.');
    const request$ = this.companyId
      ? this.companyService.updateCompany(this.companyId, this.company)
      : this.companyService.addCompany(this.company);

    request$.subscribe({
      next: (company) => {
        console.log(company);
        this.successMessage = this.companyId ? 'Company updated successfully!' : 'Company added successfully!';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/company']), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Failed to save company';
        this.liveAnnouncer.announceError(this.errorMessage);
        this.cdr.detectChanges();
      }
    });
  }

  onReset(){
    this.company = {
      id: 0,
      companyName: '',
      turnover: 0,
      ceo: '',
      briefWriteup: '',
      boardOfDirectors: '',
      sectorId: 0
    };
    this.dropDownTitle = 'Please Choose a Sector';
  }

  onSectorClick(sector:Sector){
    this.dropDownTitle = sector.name;
    this.company.sectorId = sector.id;
  }

  private syncSelectedSectorTitle(){
    const selectedSector = this.sectors.find(sector => sector.id === this.company.sectorId);
    this.dropDownTitle = selectedSector?.name || (this.companyId ? this.dropDownTitle : 'Please Choose a Sector');
  }

}
