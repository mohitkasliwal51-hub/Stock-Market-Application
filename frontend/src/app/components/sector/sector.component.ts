import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Company } from 'src/app/models/company-model';
import { Sector } from 'src/app/models/sector-model';
import { AuthService } from 'src/app/services/auth.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { SectorService } from 'src/app/services/sector.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-sector',
  templateUrl: './sector.component.html',
  styleUrls: ['./sector.component.css'],
  standalone: true,
  imports: [CommonModule, NavbarComponent]
})
export class SectorComponent implements OnInit {

  public state:string;
  public sectors:Sector[];
  public companies:Company[];
  public dropDownTitle:string;
  public currentSector:Sector;
  public errorMessage:string;
  public successMessage:string;

  constructor(private authService:AuthService, private sectorService:SectorService, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService){
    this.state="";
    this.sectors=[];
    this.companies=[];
    this.dropDownTitle="Please select sector";
    this.currentSector = {
      "id": 0,
      "name": "",
      "brief":""
    }
    this.errorMessage = '';
    this.successMessage = '';
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.sectorService.getAllSectors().subscribe(allSectors => {
      this.sectors=allSectors;
      this.cdr.detectChanges();
    });
  }

  onSectorClick(sector:Sector){
    this.currentSector = sector;
    this.dropDownTitle = sector.name;
  }

  onLoadComapnies(){
    this.errorMessage = '';
    this.successMessage = '';
    if(this.dropDownTitle==="Please select sector"){
      this.errorMessage = 'Please select a sector';
      this.liveAnnouncer.announceError(this.errorMessage);
    } else{
      this.liveAnnouncer.announceStatus('Loading companies for selected sector.');
      this.sectorService.getCompaniesBySector(this.currentSector.id).subscribe( allCompanies => {
        this.companies = allCompanies;
        this.successMessage = `${allCompanies.length} compan${allCompanies.length === 1 ? 'y' : 'ies'} found.`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.cdr.detectChanges();
      })
    }
  }

}
