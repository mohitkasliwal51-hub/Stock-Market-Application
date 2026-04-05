import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Company } from 'src/app/models/company-model';
import { Exchange } from 'src/app/models/exchange-model';
import { StockPrice } from 'src/app/models/stock-price-model';
import { AuthService } from 'src/app/services/auth.service';
import { CompanyService } from 'src/app/services/company.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { StockPriceService } from 'src/app/services/stock-price.service';
import * as Highcharts from 'highcharts';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-comparison',
  templateUrl: './comparison.component.html',
  styleUrls: ['./comparison.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent]
})
export class ComparisonComponent implements OnInit {

  public state:string;
  public stockPrices:StockPrice[][];
  public companies:Company[];
  public exchanges:Exchange[];
  public companyTitle:string;
  public exchangeTitle:string;
  public companySelected:Company;
  public exchangeSelected:Exchange;
  public fromTime:string;
  public toTime:string;
  public errorMessage:string;
  public successMessage:string;
  public isLoading:boolean;

  highcharts = Highcharts;
  chartOptions: Highcharts.Options;

  constructor(private authService:AuthService, private companyService:CompanyService, private exchangeService:ExchangeService, private stockPriceService:StockPriceService, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService) {
    this.state="";
    this.stockPrices = [];
    this.companyTitle="Please choose a company";
    this.exchangeTitle="Please choose a stock exchange";
    this.companies=[];
    this.exchanges=[];
    this.fromTime="";
    this.toTime="";
    this.errorMessage = '';
    this.successMessage = '';
    this.isLoading = false;
    this.companySelected = {
      "id": 0,
      "companyName": "",
      "turnover": 0,
      "ceo": "",
      "boardOfDirectors": "",
      "sectorId": 0,
      "briefWriteup": ""
    }
    this.exchangeSelected = {
      "id": 0,
      "name": "",
      "brief": "",
      "remarks": "",
      "address": {
          "id": 0,
          "street": "",
          "city": "",
          "country": "",
          "zipCode": 0
      }
    }

    this.chartOptions = {
      title: {
        text: "Stock Value Comparison"
      },
      xAxis: {
        labels: {
          formatter: function () {
            var label = this.axis.defaultLabelFormatter.call(this).replace(" ","").replace("k","000").replace(" ","").replace(" ","").replace("M", "000000");
            var date: Date = new Date(parseInt(label));
            return date.getDate()+"/"+date.getMonth()+"/"+date.getFullYear()+"\n"+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
          }
        }
      },
      yAxis: {
        title: {
          text: "Stock Price"
        }
      },
      series: [],
      legend: {
        title: {
            text: 'Stock<br/><span style="font-size: 9px; color: #666; font-weight: normal">(Click to hide)</span>',
            style: {
                fontStyle: 'italic'
            }
        },
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -10,
        y: 100
      }
    }

  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.companyService.getAllCompanies().subscribe(companies => {
      this.companies = companies;
      this.cdr.detectChanges();
    });
    this.exchangeService.getAllExchanges().subscribe(exchanges => {
      this.exchanges = exchanges;
      this.cdr.detectChanges();
    });
  }

  onCompanyClick(company:Company){
    this.companyTitle = company.companyName || company.name || '';
    this.companySelected = company;
  }

  onExchangeClick(exchange:Exchange){
    this.exchangeTitle = exchange.name;
    this.exchangeSelected = exchange;
  }

  onSubmit(){
    this.errorMessage = '';
    this.successMessage = '';

    if (this.companySelected.id === 0) {
      this.errorMessage = 'Please choose a company';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (this.exchangeSelected.id === 0) {
      this.errorMessage = 'Please choose a stock exchange';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (!this.fromTime || !this.toTime) {
      this.errorMessage = 'Please choose both from and to date-time values';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (new Date(this.fromTime).valueOf() >= new Date(this.toTime).valueOf()) {
      this.errorMessage = 'From Date Time must be earlier than To Date Time';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.isLoading = true;
    this.liveAnnouncer.announceStatus('Loading comparison data.');
    const fromTime = `${this.fromTime}.000+05:30`;
    const toTime = `${this.toTime}.000+05:30`;
    this.stockPriceService.getStockPrices(this.companySelected.id, this.exchangeSelected.id, fromTime, toTime).subscribe({
      next: (stockPrices) => {
        this.isLoading = false;
        if(stockPrices.length){
          this.stockPrices[this.stockPrices.length] = stockPrices;
          console.log(this.stockPrices);
          this.chartOptions.series?.push(this.getPriceSeries(stockPrices));
          Highcharts.chart('chart-container', this.chartOptions);
          this.successMessage = 'Comparison loaded successfully';
          this.liveAnnouncer.announceSuccess(this.successMessage);
        } else{
          this.errorMessage = 'No data found for the requested period';
          this.liveAnnouncer.announceError(this.errorMessage);
        }
        this.cdr.detectChanges();
        this.onReset();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err?.error?.message || err?.error?.error?.message || 'Failed to load comparison data';
        this.liveAnnouncer.announceError(this.errorMessage);
        this.cdr.detectChanges();
      }
    });
  }

  onReset(){
    this.companySelected = {
      "id": 0,
      "companyName": "",
      "turnover": 0,
      "ceo": "",
      "boardOfDirectors": "",
      "sectorId": 0,
      "briefWriteup": ""
    }
    this.exchangeSelected = {
      "id": 0,
      "name": "",
      "brief": "",
      "remarks": "",
      "address": {
          "id": 0,
          "street": "",
          "city": "",
          "country": "",
          "zipCode": 0
      }
    }
    this.companyTitle='Please choose a company';
    this.exchangeTitle='Please choose a stock exchange';
    this.fromTime = '';
    this.toTime = '';
  }

  getPriceSeries(stockPrices: StockPrice[]):Highcharts.SeriesOptionsType{
    var seriesData: number[][] = [];
    for(var i=0;i<stockPrices.length;i++){
      seriesData[i] = [new Date(stockPrices[i].timestamp).valueOf(), stockPrices[i].price];
    }
    return {
      name: `Stock #${stockPrices[0].stockId}`,
      data: seriesData,
      type: "line"
    }
  }

}
