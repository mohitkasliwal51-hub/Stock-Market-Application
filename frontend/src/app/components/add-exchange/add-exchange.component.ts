import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
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

  constructor(private authService:AuthService, private exchangeService:ExchangeService, private router: Router, private activatedRoute:ActivatedRoute) {
    this.state="";
    this.exchangeId = this.activatedRoute.snapshot.params["id"];
    this.exchange={
      "id":0,
      "name":"",
      "brief": "",
      "address":{
          "street": "",
          "city": "",
          "country": "",
          "zipCode": 0
      },
      "remarks":"Remark 3"
    }
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    if (this.exchangeId) {
      this.exchangeService.getExchangeById(this.exchangeId).subscribe(exchange => {
        this.exchange = exchange;
      });
    }
  }

  addExchange(){
    if (this.exchangeId) {
      this.exchangeService.updateExchange(this.exchangeId, this.exchange).subscribe(updatedExchange => {
        console.log("Exchange Updated");
        console.log(updatedExchange);
      });
    } else {
      this.exchangeService.addExchange(this.exchange).subscribe(exchange => {
        console.log("Exchange Added");
        console.log(exchange);
      });
    }
    this.router.navigate(['/exchange']);
  }

  onReset(){
    this.exchange = {
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
