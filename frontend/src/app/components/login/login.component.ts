import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
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
  public successMessage:string;
  public isLoading:boolean;

  constructor(private authService:AuthService, private router:Router, private liveAnnouncer: LiveAnnouncerService) {
    this.state = "unauthorized";
    this.username="";
    this.password="";
    this.errorMessage="";
    this.successMessage="";
    this.isLoading=false;
  }

  ngOnInit(): void {
  }

  onSubmit(event?:Event){
    event?.preventDefault();
    this.errorMessage = "";
    this.successMessage = "";

    if (!this.username.trim() || !this.password.trim()) {
      this.errorMessage = 'Username and password are required';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.isLoading = true;
    this.liveAnnouncer.announceStatus('Signing in.');
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Login successful';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Invalid credentials';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

}
