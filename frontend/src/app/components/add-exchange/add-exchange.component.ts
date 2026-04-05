import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from "@angular/router"
import { Exchange } from 'src/app/models/exchange-model';
import { AuthService } from 'src/app/services/auth.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-add-exchange',
  templateUrl: './add-exchange.component.html',
  styleUrls: ['./add-exchange.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class AddExchangeComponent implements OnInit {

  public state:string;
  public exchange:Exchange;
  public exchangeId:number;
  public errorMessage:string = '';
  public successMessage:string = '';
  public isLoading:boolean = false;

  constructor(private authService:AuthService, private exchangeService:ExchangeService, private router: Router, private activatedRoute:ActivatedRoute, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService) {
    this.state="";
    this.exchangeId = Number(this.activatedRoute.snapshot.params["id"] || 0);
    this.exchange = this.createEmptyExchange();
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    if (this.exchangeId) {
      this.exchangeService.getExchangeById(this.exchangeId).subscribe(exchange => {
        this.exchange = {
          id: Number(exchange.id || 0),
          name: exchange.name || '',
          brief: exchange.brief || '',
          remarks: exchange.remarks || '',
          address: {
            id: exchange.address?.id,
            street: exchange.address?.street || '',
            city: exchange.address?.city || '',
            country: exchange.address?.country || '',
            zipCode: Number(exchange.address?.zipCode || 0)
          }
        };
        this.cdr.detectChanges();
      });
    }
  }

  addExchange(){
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.exchange.name?.trim() || !this.exchange.brief?.trim() || !this.exchange.remarks?.trim()) {
      this.errorMessage = 'Please fill all required exchange details';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (!this.exchange.address.street?.trim() || !this.exchange.address.city?.trim() || !this.exchange.address.country?.trim()) {
      this.errorMessage = 'Please complete address details';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if ((this.exchange.address.zipCode || 0) <= 0) {
      this.errorMessage = 'Zip code must be greater than zero';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.isLoading = true;
    this.liveAnnouncer.announceStatus(this.exchangeId ? 'Updating exchange details.' : 'Submitting exchange details.');
    const request$ = this.exchangeId
      ? this.exchangeService.updateExchange(this.exchangeId, this.exchange)
      : this.exchangeService.addExchange(this.exchange);

    request$.subscribe({
      next: (exchange) => {
        console.log(exchange);
        this.successMessage = this.exchangeId ? 'Exchange updated successfully!' : 'Exchange added successfully!';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.cdr.detectChanges();
        setTimeout(() => this.router.navigate(['/exchange']), 1500);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Failed to save exchange';
        this.liveAnnouncer.announceError(this.errorMessage);
        this.cdr.detectChanges();
      }
    });
  }

  onReset(){
    this.exchange = this.createEmptyExchange();
  }

  private createEmptyExchange(): Exchange {
    return {
      id: 0,
      name: '',
      brief: '',
      address: {
        street: '',
        city: '',
        country: '',
        zipCode: 0
      },
      remarks: 'Remark 3'
    };
  }

}
