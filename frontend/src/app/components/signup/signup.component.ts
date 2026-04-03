import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css'],
  standalone: true,
  imports: [CommonModule, NavbarComponent]
})
export class SignupComponent implements OnInit {

  public state:string

  constructor(private authService:AuthService) {
    this.state = "unauthorized";
   }

  ngOnInit(): void {
  }

}
