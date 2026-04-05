import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from "@angular/router"
import { Exchange } from 'src/app/models/exchange-model';
import { AuthService } from 'src/app/services/auth.service';
import { ExchangeService } from 'src/app/services/exchange.service';
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

  constructor(private authService:AuthService, private exchangeService:ExchangeService, private router: Router, private activatedRoute:ActivatedRoute, private cdr: ChangeDetectorRef) {
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
    if (this.exchangeId) {
      this.exchangeService.updateExchange(this.exchangeId, this.exchange).subscribe(updatedExchange => {
        console.log("Exchange Updated");
        console.log(updatedExchange);
        this.cdr.detectChanges();
        this.router.navigate(['/exchange']);
      });
    } else {
      this.exchangeService.addExchange(this.exchange).subscribe(exchange => {
        console.log("Exchange Added");
        console.log(exchange);
        this.cdr.detectChanges();
        this.router.navigate(['/exchange']);
      });
    }
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
