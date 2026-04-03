import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: true,
  imports: [CommonModule, NavbarComponent]
})
export class HomeComponent implements OnInit {

  public state:string;

  constructor(private authService:AuthService) {
    this.state=authService.getCurrentUserRole();
  }

  ngOnInit(): void {
  }

}
