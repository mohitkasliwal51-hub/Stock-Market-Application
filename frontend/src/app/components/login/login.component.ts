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
  public errorMessage:string;

  constructor(private authService:AuthService, private router:Router) {
    this.state = "unauthorized";
    this.username="";
    this.password="";
    this.errorMessage="";
  }

  ngOnInit(): void {
  }

  onSubmit(event?:Event){
    event?.preventDefault();
    this.errorMessage = "";
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Invalid credentials';
      }
    });
  }

}
