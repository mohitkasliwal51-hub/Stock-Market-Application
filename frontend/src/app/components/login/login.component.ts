import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class LoginComponent implements OnInit {

  public state:string
  public username:string;
  public password:string;

  constructor(private authService:AuthService, private router:Router) {
    this.state = "unauthorized";
    this.username="";
    this.password="";
  }

  ngOnInit(): void {
  }

  onSubmit(){
    this.authService.authenticate(this.username, this.password);
    this.router.navigate(['/']);
  }

}
