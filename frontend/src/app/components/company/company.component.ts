import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Company } from 'src/app/models/company-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
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
  public pattern:string;

  constructor(private authService:AuthService, private companyService:CompanyService) {
    this.state="";
    this.companies=[];
    this.pattern="";
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.getAllCompanies();
  }

  public getAllCompanies(){
    this.companyService.getAllCompanies().subscribe( allCompanies => {
      this.companies = allCompanies;
    });
  }

  public getCompanyByPattern(){
    if(this.pattern != ""){
      this.companyService.getCompanyByName(this.pattern).subscribe( foundCompanies => {
        this.companies = foundCompanies;
      });
    } else {
      this.getAllCompanies();
    }
  }

  public deleteCompany(id:number){
    this.companyService.deleteCompany(id).subscribe( company =>{
      console.log(company);
      this.getAllCompanies();
    })
  }

}
