import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
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

@Component({
  selector: 'app-investor-desk',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
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
  public availableStocks: Stock[];
  public stockOptions: Array<{ id: number; label: string }>;

  public createWalletAmount: number;
  public depositAmount: number;
  public portfolioName: string;

  public stockId: number;
  public quantity: number;
  public side: OrderSide;
  public orderType: OrderType;
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
    private liveAnnouncer: LiveAnnouncerService
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
    this.availableStocks = [];
    this.stockOptions = [];

    this.createWalletAmount = 0;
    this.depositAmount = 0;
    this.portfolioName = 'Primary Portfolio';

    this.stockId = 0;
    this.quantity = 1;
    this.side = 'BUY';
    this.orderType = 'MARKET';
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
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Failed to fetch your profile';
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
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Profile update failed';
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
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Wallet creation failed';
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
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Deposit failed';
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
        this.successMessage = 'Portfolio created successfully';
        this.liveAnnouncer.announceSuccess(this.successMessage);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Portfolio creation failed';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

  onPortfolioSelected(portfolioId: number | string): void {
    this.selectedPortfolioId = Number(portfolioId);
    if (!this.selectedPortfolioId) {
      this.positions = [];
      return;
    }
    this.portfolioService.getPositions(this.selectedPortfolioId).subscribe({
      next: (positions) => {
        this.positions = positions;
      },
      error: (_err) => {
        this.positions = [];
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
      this.errorMessage = 'Please select a stock and quantity';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    const payload: CreateOrderRequest = {
      userId: this.profile.id,
      portfolioId: this.selectedPortfolioId,
      stockId: this.stockId,
      quantity: this.quantity,
      orderType: this.orderType,
      side: this.side,
      idempotencyKey: this.createIdempotencyKey()
    };

    if (this.orderType === 'LIMIT' && this.orderPrice) {
      payload.orderPrice = this.orderPrice;
    }
    if ((this.orderType === 'STOP_LOSS' || this.orderType === 'TAKE_PROFIT') && this.triggerPrice) {
      payload.triggerPrice = this.triggerPrice;
    }
    if (this.orderType === 'TRAILING_STOP' && this.trailAmount) {
      payload.trailAmount = this.trailAmount;
    }

    this.orderService.createOrder(payload).subscribe({
      next: (order) => {
        this.successMessage = `Order #${order.id} created with status ${order.status}`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.refreshOrders(this.profile!.id);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Order placement failed';
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
        this.errorMessage = err?.error?.message || 'Order cancellation failed';
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
        this.availableStocks = data.stocks;
        this.stockOptions = this.buildStockOptions(data.stocks, data.companies);
        if (this.portfolios.length && this.selectedPortfolioId === 0) {
          this.selectedPortfolioId = this.portfolios[0].id;
          this.positions = this.portfolios[0].positions || [];
        }
        this.refreshWallet(userId);
      },
      error: (err) => {
        this.errorMessage = err?.error?.message || 'Failed to load investor desk data';
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
      },
      error: (_err) => {
        this.wallet = null;
      }
    });
  }

  private refreshOrders(userId: number): void {
    this.orderService.listOrders(userId).subscribe({
      next: (orders) => {
        this.orders = orders;
      }
    });
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
