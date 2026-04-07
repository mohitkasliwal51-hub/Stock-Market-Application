import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { NavbarComponent } from '../navbar/navbar.component';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { MarketLiveService, LivePrice, MarketStatus } from 'src/app/services/market-live.service';
import { CreateOrderRequest, Order, OrderService, OrderSide, OrderType } from 'src/app/services/order.service';
import { Portfolio, PortfolioPosition, PortfolioService } from 'src/app/services/portfolio.service';
import { UserProfile, UserProfileService } from 'src/app/services/user-profile.service';
import { Wallet, WalletService } from 'src/app/services/wallet.service';
import { Stock } from 'src/app/models/stock-model';
import { Company } from 'src/app/models/company-model';
import { StockService } from 'src/app/services/stock.service';
import { CompanyService } from 'src/app/services/company.service';

type OrderFlow = 'MARKET_BUY' | 'LIMIT_BUY' | 'STOP_LOSS_SELL' | 'TAKE_PROFIT_SELL' | 'TRAILING_STOP_SELL';

@Component({
  selector: 'app-investor-desk',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarComponent],
  templateUrl: './investor-desk.component.html',
  styleUrls: ['./investor-desk.component.css']
})
export class InvestorDeskComponent implements OnInit {

  public state: string;
  public profile: UserProfile | null;
  public wallet: Wallet | null;
  public portfolios: Portfolio[];
  public selectedPortfolioId: number;
  public positions: PortfolioPosition[];
  public orders: Order[];
  public selectedOrderId: number | null;
  public executionHistoryText: string;
  public marketStatus: MarketStatus | null;
  public livePrices: LivePrice[];
  public allStockOptions: Array<{ id: number; label: string }>;
  public visibleStockOptions: Array<{ id: number; label: string }>;

  public createWalletAmount: number;
  public depositAmount: number;
  public portfolioName: string;

  public stockId: number;
  public quantity: number;
  public side: OrderSide;
  public orderFlow: OrderFlow;
  public orderPrice: number | null;
  public triggerPrice: number | null;
  public trailAmount: number | null;
  public profileEmail: string;
  public profilePassword: string;

  public isLoading: boolean;
  public errorMessage: string;
  public successMessage: string;

  constructor(
    private profileService: UserProfileService,
    private walletService: WalletService,
    private portfolioService: PortfolioService,
    private orderService: OrderService,
    private marketLiveService: MarketLiveService,
    private stockService: StockService,
    private companyService: CompanyService,
    private liveAnnouncer: LiveAnnouncerService,
    private changeDetectorRef: ChangeDetectorRef
  ) {
    this.state = 'user';
    this.profile = null;
    this.wallet = null;
    this.portfolios = [];
    this.selectedPortfolioId = 0;
    this.positions = [];
    this.orders = [];
    this.selectedOrderId = null;
    this.executionHistoryText = '';
    this.marketStatus = null;
    this.livePrices = [];
    this.allStockOptions = [];
    this.visibleStockOptions = [];

    this.createWalletAmount = 0;
    this.depositAmount = 0;
    this.portfolioName = 'Primary Portfolio';

    this.stockId = 0;
    this.quantity = 1;
    this.side = 'BUY';
    this.orderFlow = 'MARKET_BUY';
    this.orderPrice = null;
    this.triggerPrice = null;
    this.trailAmount = null;
    this.profileEmail = '';
    this.profilePassword = '';

    this.isLoading = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  ngOnInit(): void {
    const cached = this.profileService.getCachedProfile();
    if (cached) {
      this.profile = cached;
      this.loadDashboard(cached.id);
    }

    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileEmail = profile.email;
        this.loadDashboard(profile.id);
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to fetch your profile. Please refresh.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  updateProfile(): void {
    this.clearMessages();

    const payload: { email?: string; password?: string } = {};
    if (this.profileEmail.trim()) {
      payload.email = this.profileEmail.trim();
    }
    if (this.profilePassword.trim()) {
      payload.password = this.profilePassword.trim();
    }

    if (!payload.email && !payload.password) {
      this.errorMessage = 'Please provide email or password to update';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.profileService.updateProfile(payload).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.profileEmail = profile.email;
        this.profilePassword = '';
        this.successMessage = 'Profile updated successfully';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Profile update failed. Please try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  createWallet(): void {
    if (!this.profile) {
      return;
    }
    this.clearMessages();
    this.walletService.createWallet(this.profile.id, this.createWalletAmount).subscribe({
      next: (wallet) => {
        this.wallet = wallet;
        this.successMessage = 'Wallet created successfully';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Wallet creation failed. Please try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  depositToWallet(): void {
    if (!this.profile) {
      return;
    }
    if (this.depositAmount <= 0) {
      this.errorMessage = 'Deposit amount should be greater than zero';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    const referenceId = `DEP-${Date.now()}`;
    this.clearMessages();
    this.walletService.deposit(this.profile.id, this.depositAmount, referenceId, 'Self top-up').subscribe({
      next: (wallet) => {
        this.wallet = wallet;
        this.successMessage = 'Amount deposited successfully';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.depositAmount = 0;
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Deposit failed. Please try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  createPortfolio(): void {
    if (!this.profile) {
      return;
    }
    if (!this.portfolioName.trim()) {
      this.errorMessage = 'Portfolio name is required';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }
    this.clearMessages();
    this.portfolioService.createPortfolio(this.profile.id, this.portfolioName).subscribe({
      next: (portfolio) => {
        this.portfolios = [portfolio, ...this.portfolios];
        this.selectedPortfolioId = portfolio.id;
        this.positions = portfolio.positions || [];
        this.refreshVisibleStockOptions();
        this.successMessage = 'Portfolio created successfully';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Portfolio creation failed. Please try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  onPortfolioSelected(portfolioId: number | string): void {
    this.selectedPortfolioId = Number(portfolioId);
    if (!this.selectedPortfolioId) {
      this.positions = [];
      this.refreshVisibleStockOptions();
      return;
    }
    this.portfolioService.getPositions(this.selectedPortfolioId).subscribe({
      next: (positions) => {
        this.positions = positions;
        this.refreshVisibleStockOptions();
        this.changeDetectorRef.detectChanges();
      },
      error: (_err) => {
        this.positions = [];
        this.refreshVisibleStockOptions();
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  placeOrder(): void {
    if (!this.profile) {
      return;
    }
    this.clearMessages();

    if (this.selectedPortfolioId <= 0) {
      this.errorMessage = 'Please select a portfolio';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (this.stockId <= 0 || this.quantity <= 0) {
      this.errorMessage = this.isSellFlow() ? 'Please select a stock and quantity to sell' : 'Please select a stock and quantity';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    if (this.isSellFlow()) {
      const holdingQuantity = this.getHoldingQuantity(this.stockId);
      if (holdingQuantity <= 0) {
        this.errorMessage = 'Select a stock from your current holdings for a stop loss sell flow';
        this.liveAnnouncer.announceError(this.errorMessage);
        return;
      }
      if (this.quantity > holdingQuantity) {
        this.errorMessage = `You can only sell up to ${holdingQuantity} shares for the selected stock`;
        this.liveAnnouncer.announceError(this.errorMessage);
        return;
      }
    }

    const selectedFlowType = this.getSelectedOrderType();

    const payload: CreateOrderRequest = {
      userId: this.profile.id,
      portfolioId: this.selectedPortfolioId,
      stockId: this.stockId,
      quantity: this.quantity,
      orderType: selectedFlowType,
      side: this.getSelectedOrderSide(),
      idempotencyKey: this.createIdempotencyKey()
    };

    if (this.orderFlow === 'LIMIT_BUY') {
      if (!this.orderPrice || this.orderPrice <= 0) {
        this.errorMessage = 'Limit price is required for LIMIT flow';
        this.liveAnnouncer.announceError(this.errorMessage);
        return;
      }
      payload.orderPrice = this.orderPrice;
    }

    if (this.orderFlow === 'STOP_LOSS_SELL') {
      if (!this.triggerPrice || this.triggerPrice <= 0) {
        this.errorMessage = 'Trigger price is required when using Stop Loss or Take Profit';
        this.liveAnnouncer.announceError(this.errorMessage);
        return;
      }
      payload.triggerPrice = this.triggerPrice;
    }

    if (this.orderFlow === 'TAKE_PROFIT_SELL') {
      if (!this.triggerPrice || this.triggerPrice <= 0) {
        this.errorMessage = 'Trigger price is required when using Stop Loss or Take Profit';
        this.liveAnnouncer.announceError(this.errorMessage);
        return;
      }
      payload.orderPrice = this.triggerPrice;
    }

    if (this.orderFlow === 'TRAILING_STOP_SELL') {
      if (!this.trailAmount || this.trailAmount <= 0) {
        this.errorMessage = 'Trail amount is required for Trailing Stop';
        this.liveAnnouncer.announceError(this.errorMessage);
        return;
      }
      payload.trailAmount = this.trailAmount;
    }

    this.orderService.createOrder(payload).subscribe({
      next: (order) => {
        this.successMessage = `Order #${order.id} created with status ${order.status}`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.refreshOrders(this.profile!.id);
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Order placement failed. Please try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  cancelOrder(orderId: number): void {
    this.clearMessages();
    this.orderService.cancelOrder(orderId).subscribe({
      next: (order) => {
        this.successMessage = `Order #${order.id} cancelled`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
        if (this.profile) {
          this.refreshOrders(this.profile.id);
          this.refreshWallet(this.profile.id);
        }
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Order cancellation failed. Please try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  loadExecutionHistory(orderId: number): void {
    this.selectedOrderId = orderId;
    this.executionHistoryText = 'Loading execution history...';
    this.orderService.getExecutionHistory(orderId).subscribe({
      next: (events) => {
        if (!events.length) {
          this.executionHistoryText = 'No execution events available yet.';
          return;
        }
        this.executionHistoryText = events
          .map(event => `${event.eventAt} | ${event.eventType} | ${event.note || ''}`)
          .join('\n');
      },
      error: (_err) => {
        this.executionHistoryText = 'Failed to load execution history.';
      }
    });
  }

  canCancelOrder(status: string): boolean {
    return status === 'CREATED' || status === 'OPEN' || status === 'RESERVED' || status === 'TRIGGER_PENDING';
  }

  isOrderFormReady(): boolean {
    return this.getOrderChecklist().length === 0;
  }

  getOrderChecklist(): string[] {
    const checks: string[] = [];

    if (this.selectedPortfolioId <= 0) {
      checks.push('Select a portfolio');
    }
    if (this.stockId <= 0) {
      checks.push(this.isSellFlow() ? 'Select a stock from your holdings' : 'Select a stock');
    }
    if (this.quantity <= 0) {
      checks.push(this.isSellFlow() ? 'Enter quantity to sell greater than 0' : 'Enter quantity greater than 0');
    }
    if (this.isSellFlow() && this.stockId > 0) {
      const holdingQuantity = this.getHoldingQuantity(this.stockId);
      if (holdingQuantity <= 0) {
        checks.push('Choose a stock you already own');
      } else if (this.quantity > holdingQuantity) {
        checks.push(`Quantity to sell cannot exceed your holding of ${holdingQuantity}`);
      }
    }
    if (this.orderFlow === 'LIMIT_BUY' && (!this.orderPrice || this.orderPrice <= 0)) {
      checks.push('Enter limit price for LIMIT flow');
    }
    if ((this.orderFlow === 'STOP_LOSS_SELL' || this.orderFlow === 'TAKE_PROFIT_SELL')
      && (!this.triggerPrice || this.triggerPrice <= 0)) {
      checks.push('Enter trigger price for selected optional feature');
    }
    if (this.orderFlow === 'TRAILING_STOP_SELL' && (!this.trailAmount || this.trailAmount <= 0)) {
      checks.push('Enter trail amount for Trailing Stop');
    }

    return checks;
  }

  getOrderFlowSummary(): string {
    if (this.orderFlow === 'MARKET_BUY') {
      return 'Flow selected: Market buy order for a stock you want to purchase now.';
    }
    if (this.orderFlow === 'LIMIT_BUY') {
      return 'Flow selected: Limit buy order with your chosen maximum price.';
    }
    if (this.orderFlow === 'STOP_LOSS_SELL') {
      return 'Flow selected: Stop Loss sell order. When price reaches the threshold, the selected quantity will be sold and wallet/transactions will update automatically.';
    }
    if (this.orderFlow === 'TAKE_PROFIT_SELL') {
      return 'Flow selected: Take Profit sell order. When price reaches the threshold, the selected quantity will be sold and wallet/transactions will update automatically.';
    }
    return 'Flow selected: Trailing Stop sell order for protecting gains while the market moves.';
  }

  getLaterFeatureGuidance(): string {
    if (this.orderFlow === 'MARKET_BUY' || this.orderFlow === 'LIMIT_BUY') {
      return 'Later usage: if you later want protection for shares you already own, create a new order, switch the flow to Stop Loss, Take Profit, or Trailing Stop, then select the stock from your holdings.';
    }
    return 'This flow protects a position you already own. You can reuse the same pattern later by choosing the protective sell flow and selecting the quantity to sell.';
  }

  onOrderFlowChanged(): void {
    this.stockId = 0;
    this.orderPrice = null;
    this.triggerPrice = null;
    this.trailAmount = null;
    this.refreshVisibleStockOptions();
  }

  private getSelectedOrderType(): OrderType {
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

  private getSelectedOrderSide(): OrderSide {
    return this.isSellFlow() ? 'SELL' : 'BUY';
  }

  public isSellFlow(): boolean {
    return this.orderFlow.endsWith('_SELL');
  }

  private getHoldingQuantity(stockId: number): number {
    const position = this.positions.find(item => item.stockId === stockId);
    return position?.quantity || 0;
  }

  private loadDashboard(userId: number): void {
    this.isLoading = true;
    forkJoin({
      marketStatus: this.marketLiveService.getStatus(),
      livePrices: this.marketLiveService.getLivePrices(),
      portfolios: this.portfolioService.listByUser(userId),
      orders: this.orderService.listOrders(userId),
      stocks: this.stockService.getAllStocks(),
      companies: this.companyService.getAllCompanies()
    }).subscribe({
      next: (data) => {
        this.marketStatus = data.marketStatus;
        this.livePrices = data.livePrices;
        this.portfolios = data.portfolios;
        this.orders = data.orders;
        this.allStockOptions = this.buildStockOptions(data.stocks, data.companies);
        this.refreshVisibleStockOptions();
        if (this.portfolios.length && this.selectedPortfolioId === 0) {
          this.selectedPortfolioId = this.portfolios[0].id;
          this.positions = this.portfolios[0].positions || [];
          this.refreshVisibleStockOptions();
        }
        this.refreshWallet(userId);
        this.changeDetectorRef.detectChanges();
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Failed to load dashboard. Please refresh.';
        this.liveAnnouncer.announceError(this.errorMessage);
      },
      complete: () => {
        this.isLoading = false;
      }
    });
  }

  private refreshWallet(userId: number): void {
    this.walletService.getWallet(userId).subscribe({
      next: (wallet) => {
        this.wallet = wallet;
        this.changeDetectorRef.detectChanges();
      },
      error: (_err) => {
        this.wallet = null;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  private refreshOrders(userId: number): void {
    this.orderService.listOrders(userId).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.changeDetectorRef.detectChanges();
      }
    });
  }

  private refreshVisibleStockOptions(): void {
    if (this.isSellFlow()) {
      const holdings = new Map(this.positions.map(position => [position.stockId, position.quantity]));
      this.visibleStockOptions = this.allStockOptions
        .filter(option => holdings.has(option.id))
        .map(option => {
          const quantity = holdings.get(option.id) || 0;
          return {
            id: option.id,
            label: `${option.label} - Holding ${quantity}`
          };
        });
      return;
    }

    this.visibleStockOptions = [...this.allStockOptions];
  }

  private buildStockOptions(stocks: Stock[], companies: Company[]): Array<{ id: number; label: string }> {
    const companyById = new Map<number, string>();
    for (const company of companies) {
      const name = company.companyName || company.name || `Company ${company.id}`;
      companyById.set(company.id, name);
    }

    return stocks.map(stock => {
      const companyName = companyById.get(stock.companyId) || `Company ${stock.companyId}`;
      return {
        id: stock.id,
        label: `${stock.stockCode} - ${companyName}`
      };
    });
  }

  private createIdempotencyKey(): string {
    return `ord-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }
}
