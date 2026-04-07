import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { MarketLiveService, LivePrice, MarketStatus } from 'src/app/services/market-live.service';
import { CreateOrderRequest, OrderService, OrderSide, OrderType } from 'src/app/services/order.service';
import { Portfolio, PortfolioPosition, PortfolioService } from 'src/app/services/portfolio.service';
import { UserProfile, UserProfileService } from 'src/app/services/user-profile.service';
import { Stock } from 'src/app/models/stock-model';
import { Company } from 'src/app/models/company-model';
import { StockService } from 'src/app/services/stock.service';
import { CompanyService } from 'src/app/services/company.service';

interface StockOption {
  id: number;
  label: string;
}

type OrderFlow = 'MARKET_BUY' | 'LIMIT_BUY' | 'STOP_LOSS_SELL' | 'TAKE_PROFIT_SELL' | 'TRAILING_STOP_SELL';

const ORDER_SUCCESS_REDIRECT_DELAY_MS = 1500;

@Component({
  selector: 'app-place-order',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './place-order.component.html',
  styleUrls: ['./place-order.component.css']
})
export class PlaceOrderComponent implements OnInit {
  public state: string;
  public profile: UserProfile | null;
  public portfolios: Portfolio[];
  public positions: PortfolioPosition[];
  public portfolioId: number;
  public stockId: number;
  public quantity: number;
  public orderFlow: OrderFlow;
  public orderPrice: number | null;
  public triggerPrice: number | null;
  public trailAmount: number | null;
  public selectedOrderSide: OrderSide;
  public livePrices: LivePrice[];
  public marketStatus: MarketStatus | null;
  public orderPreview: string;
  public stockOptions: StockOption[];
  public visibleStockOptions: StockOption[];
  public isLoading: boolean;
  public errorMessage: string;
  public successMessage: string;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profileService: UserProfileService,
    private portfolioService: PortfolioService,
    private stockService: StockService,
    private companyService: CompanyService,
    private marketLiveService: MarketLiveService,
    private orderService: OrderService,
    private liveAnnouncer: LiveAnnouncerService,
    private changeDetectorRef: ChangeDetectorRef
  ) {
    this.state = 'user';
    this.profile = null;
    this.portfolios = [];
    this.positions = [];
    this.portfolioId = 0;
    this.stockId = 0;
    this.quantity = 1;
    this.orderFlow = 'MARKET_BUY';
    this.orderPrice = null;
    this.triggerPrice = null;
    this.trailAmount = null;
    this.selectedOrderSide = 'BUY';
    this.livePrices = [];
    this.marketStatus = null;
    this.orderPreview = 'Choose a flow to see how this order will work.';
    this.stockOptions = [];
    this.visibleStockOptions = [];
    this.isLoading = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  ngOnInit(): void {
    const cached = this.profileService.getCachedProfile();
    if (cached) {
      this.profile = cached;
      this.portfolioId = Number(this.route.snapshot.queryParamMap.get('portfolioId') || 0);
      this.stockId = Number(this.route.snapshot.queryParamMap.get('stockId') || 0);
      this.orderFlow = (this.route.snapshot.queryParamMap.get('flow') as OrderFlow) || 'MARKET_BUY';
      this.selectedOrderSide = this.isSellFlow() ? 'SELL' : 'BUY';
      this.loadPageData(cached.id);
    }

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.portfolioId = Number(this.route.snapshot.queryParamMap.get('portfolioId') || this.portfolioId || 0);
        this.stockId = Number(this.route.snapshot.queryParamMap.get('stockId') || this.stockId || 0);
        this.orderFlow = (this.route.snapshot.queryParamMap.get('flow') as OrderFlow) || this.orderFlow || 'MARKET_BUY';
        this.selectedOrderSide = this.isSellFlow() ? 'SELL' : 'BUY';
        this.loadPageData(profile.id);
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to fetch profile. Please refresh and try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  onPortfolioChange(value: number | string): void {
    this.portfolioId = Number(value);
    this.syncSelectedPortfolio();
    this.refreshVisibleStockOptions();
    this.changeDetectorRef.detectChanges();
  }

  onOrderFlowChange(): void {
    this.selectedOrderSide = this.isSellFlow() ? 'SELL' : 'BUY';
    this.orderPrice = null;
    this.triggerPrice = null;
    this.trailAmount = null;
    this.stockId = this.isSellFlow() && this.positions.length ? this.positions[0].stockId : 0;
    this.refreshVisibleStockOptions();
    this.updatePreview();
  }

  isSellFlow(): boolean {
    return this.orderFlow.endsWith('_SELL');
  }

  isReady(): boolean {
    return this.getChecklist().length === 0;
  }

  getChecklist(): string[] {
    const checks: string[] = [];
    if (this.portfolioId <= 0) {
      checks.push('Select a portfolio');
    }
    if (this.stockId <= 0) {
      checks.push(this.isSellFlow() ? 'Select a stock from holdings' : 'Select a stock');
    }
    if (this.quantity <= 0) {
      checks.push(this.isSellFlow() ? 'Enter quantity to sell' : 'Enter quantity');
    }
    if (this.isSellFlow() && this.stockId > 0) {
      const holdingQuantity = this.getHoldingQuantity(this.stockId);
      if (holdingQuantity <= 0) {
        checks.push('Choose a stock you already own');
      } else if (this.quantity > holdingQuantity) {
        checks.push(`Quantity cannot exceed holding of ${holdingQuantity}`);
      }
    }
    if (this.orderFlow === 'LIMIT_BUY' && (!this.orderPrice || this.orderPrice <= 0)) {
      checks.push('Add a limit price');
    }
    if ((this.orderFlow === 'STOP_LOSS_SELL' || this.orderFlow === 'TAKE_PROFIT_SELL') && (!this.triggerPrice || this.triggerPrice <= 0)) {
      checks.push('Add a threshold price');
    }
    if (this.orderFlow === 'TRAILING_STOP_SELL' && (!this.trailAmount || this.trailAmount <= 0)) {
      checks.push('Add a trail amount');
    }
    return checks;
  }

  getFlowDescription(): string {
    if (this.orderFlow === 'MARKET_BUY') {
      return 'Market buy: place an immediate buy order.';
    }
    if (this.orderFlow === 'LIMIT_BUY') {
      return 'Limit buy: buy only at or below your chosen limit price.';
    }
    if (this.orderFlow === 'STOP_LOSS_SELL') {
      return 'Stop loss sell: protect an owned position by selling when the threshold is reached.';
    }
    if (this.orderFlow === 'TAKE_PROFIT_SELL') {
      return 'Take profit sell: lock gains by selling when the threshold is reached.';
    }
    return 'Trailing stop sell: sell an owned position when the trailing threshold is reached.';
  }

  getUsageHint(): string {
    if (this.isSellFlow()) {
      return 'If you later want protection, open this page again from a portfolio holding button, choose the sell flow, then set stock, quantity, and threshold.';
    }
    return 'Later you can return here and choose a sell flow to protect a stock you already own.';
  }

  submitOrder(): void {
    if (!this.profile) {
      return;
    }
    this.clearMessages();
    if (!this.isReady()) {
      this.errorMessage = this.getChecklist()[0] || 'Complete the required fields first';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.liveAnnouncer.announceStatus('Submitting order.');

    const payload: CreateOrderRequest = {
      userId: this.profile.id,
      portfolioId: this.portfolioId,
      stockId: this.stockId,
      quantity: this.quantity,
      orderType: this.getOrderType(),
      side: this.getSelectedSide(),
      idempotencyKey: this.createIdempotencyKey()
    };

    if (this.orderFlow === 'LIMIT_BUY') {
      payload.orderPrice = this.orderPrice || undefined;
    }
    if (this.orderFlow === 'STOP_LOSS_SELL') {
      payload.triggerPrice = this.triggerPrice || undefined;
    }
    if (this.orderFlow === 'TAKE_PROFIT_SELL') {
      payload.orderPrice = this.triggerPrice || undefined;
    }
    if (this.orderFlow === 'TRAILING_STOP_SELL') {
      payload.trailAmount = this.trailAmount || undefined;
    }

    console.log('Submitting order:', payload);
    this.orderService.createOrder(payload).subscribe({
      next: (order) => {
        this.successMessage = `Order #${order.id} created successfully`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.changeDetectorRef.detectChanges();
        setTimeout(() => this.router.navigate(['/investor']), ORDER_SUCCESS_REDIRECT_DELAY_MS);
      },
      error: (err) => {
        console.error('Order creation error:', err);
        const errorMsg = err?.message || 'Failed to place order. Please try again.';
        this.errorMessage = errorMsg;
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  selectQuickFlow(flow: OrderFlow): void {
    this.orderFlow = flow;
    this.onOrderFlowChange();
  }

  private loadPageData(userId: number): void {
    this.isLoading = true;
    forkJoin({
      portfolios: this.portfolioService.listByUser(userId),
      stocks: this.stockService.getAllStocks(),
      companies: this.companyService.getAllCompanies(),
      marketStatus: this.marketLiveService.getStatus(),
      livePrices: this.marketLiveService.getLivePrices()
    }).subscribe({
      next: (data) => {
        this.portfolios = data.portfolios;
        this.marketStatus = data.marketStatus;
        this.livePrices = data.livePrices;
        this.stockOptions = this.buildStockOptions(data.stocks, data.companies);
        this.syncSelectedPortfolio();
        if (this.isSellFlow() && !this.stockId && this.positions.length) {
          this.stockId = this.positions[0].stockId;
        }
        this.refreshVisibleStockOptions();
        this.updatePreview();
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to load order page. Please refresh.';
        this.liveAnnouncer.announceError(this.errorMessage);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private refreshVisibleStockOptions(): void {
    if (this.isSellFlow()) {
      const holdings = new Map(this.positions.map(position => [position.stockId, position.quantity]));
      this.visibleStockOptions = this.stockOptions
        .filter(option => holdings.has(option.id))
        .map(option => ({ id: option.id, label: `${option.label} - Holding ${holdings.get(option.id) || 0}` }));
      return;
    }

    this.visibleStockOptions = [...this.stockOptions];
  }

  private buildStockOptions(stocks: Stock[], companies: Company[]): StockOption[] {
    const companyById = new Map<number, string>();
    for (const company of companies) {
      companyById.set(company.id, company.companyName || company.name || `Company ${company.id}`);
    }

    return stocks.map(stock => ({
      id: stock.id,
      label: `${stock.stockCode} - ${companyById.get(stock.companyId) || `Company ${stock.companyId}`}`
    }));
  }

  private getOrderType(): OrderType {
    switch (this.orderFlow) {
      case 'LIMIT_BUY':
        return 'LIMIT';
      case 'STOP_LOSS_SELL':
        return 'STOP_LOSS';
      case 'TAKE_PROFIT_SELL':
        return 'TAKE_PROFIT';
      case 'TRAILING_STOP_SELL':
        return 'TRAILING_STOP';
      default:
        return 'MARKET';
    }
  }

  private getSelectedSide(): OrderSide {
    return this.isSellFlow() ? 'SELL' : 'BUY';
  }

  private getHoldingQuantity(stockId: number): number {
    return this.positions.find(position => position.stockId === stockId)?.quantity || 0;
  }

  private syncSelectedPortfolio(): void {
    const selectedPortfolio = this.portfolios.find(portfolio => portfolio.id === this.portfolioId);
    if (selectedPortfolio) {
      this.positions = selectedPortfolio.positions || [];
      return;
    }

    if (this.portfolios.length > 0) {
      const fallbackPortfolio = this.portfolios[0];
      this.portfolioId = fallbackPortfolio.id;
      this.positions = fallbackPortfolio.positions || [];
      return;
    }

    this.positions = [];
  }

  getSelectedPortfolioName(): string {
    return this.portfolios.find(portfolio => portfolio.id === this.portfolioId)?.portfolioName || 'Selected portfolio';
  }

  private updatePreview(): void {
    this.orderPreview = this.getFlowDescription();
  }

  private createIdempotencyKey(): string {
    return `ord-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
