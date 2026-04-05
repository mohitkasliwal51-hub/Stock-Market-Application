import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class SignupComponent implements OnInit {

  public state:string
  public username:string;
  public password:string;
  public email:string;
  public errorMessage:string;
  public successMessage:string;
  public isLoading:boolean;

  constructor(private authService:AuthService, private router:Router, private liveAnnouncer: LiveAnnouncerService) {
    this.state = "unauthorized";
    this.username = "";
    this.password = "";
    this.email = "";
    this.errorMessage = "";
    this.successMessage = "";
    this.isLoading = false;
   }

  ngOnInit(): void {
  }

  onSubmit(event?:Event){
    event?.preventDefault();
    this.errorMessage = "";
    this.successMessage = "";

    if (!this.username.trim() || !this.password.trim() || !this.email.trim()) {
      this.errorMessage = 'Username, password and email are required';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.isLoading = true;
    this.liveAnnouncer.announceStatus('Creating your account.');
    this.authService.register(this.username, this.password, this.email).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Signup successful';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Signup failed';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

}
