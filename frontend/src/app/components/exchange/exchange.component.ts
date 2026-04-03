import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Exchange } from 'src/app/models/exchange-model';
import { AuthService } from 'src/app/services/auth.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-exchange',
  templateUrl: './exchange.component.html',
  styleUrls: ['./exchange.component.css'],
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent]
})
export class ExchangeComponent implements OnInit {

  public state:string;
  public exchanges:Exchange[];

  constructor(private authService:AuthService, private exchangeService:ExchangeService) {
    this.state="";
    this.exchanges=[];
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.exchangeService.getAllExchanges().subscribe( allExchanges => {
      this.exchanges = allExchanges;
    });
  }

}
