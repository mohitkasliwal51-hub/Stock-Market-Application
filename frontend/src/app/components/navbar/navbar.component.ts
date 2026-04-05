import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  standalone: true,
  imports: [CommonModule, RouterLink]
})
export class NavbarComponent implements OnInit {

  @Input() public state:string;
  public currentPage:string;

  constructor(private authService:AuthService, private router:Router) {
    this.state = "";
    this.currentPage = router.url;
  }

  ngOnInit(): void {
  }

  logout(){
    this.authService.logout();
    this.router.navigate(["/"]);
    if(this.currentPage==="/"){
      location.reload();
    }
  }

}
