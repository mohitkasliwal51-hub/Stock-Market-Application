import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core';
import { PlaceOrderComponent } from './place-order.component';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { PortfolioService } from 'src/app/services/portfolio.service';
import { StockService } from 'src/app/services/stock.service';
import { CompanyService } from 'src/app/services/company.service';
import { MarketLiveService } from 'src/app/services/market-live.service';
import { OrderService } from 'src/app/services/order.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { Portfolio } from 'src/app/services/portfolio.service';
import { Stock } from 'src/app/models/stock-model';
import { Company } from 'src/app/models/company-model';
import { MarketStatus, LivePrice } from 'src/app/services/market-live.service';
import { Order } from 'src/app/services/order.service';

describe('PlaceOrderComponent', () => {
  let component: PlaceOrderComponent;
  let fixture: ComponentFixture<PlaceOrderComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;
  let portfolioServiceSpy: jasmine.SpyObj<PortfolioService>;
  let profileServiceSpy: jasmine.SpyObj<UserProfileService>;
  let stockServiceSpy: jasmine.SpyObj<StockService>;
  let companyServiceSpy: jasmine.SpyObj<CompanyService>;
  let marketLiveServiceSpy: jasmine.SpyObj<MarketLiveService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let liveAnnouncerSpy: jasmine.SpyObj<LiveAnnouncerService>;

  beforeEach(async () => {
    orderServiceSpy = jasmine.createSpyObj('OrderService', ['createOrder']);
    portfolioServiceSpy = jasmine.createSpyObj('PortfolioService', ['listByUser']);
    profileServiceSpy = jasmine.createSpyObj('UserProfileService', ['getCachedProfile', 'getProfile']);
    stockServiceSpy = jasmine.createSpyObj('StockService', ['getAllStocks']);
    companyServiceSpy = jasmine.createSpyObj('CompanyService', ['getAllCompanies']);
    marketLiveServiceSpy = jasmine.createSpyObj('MarketLiveService', ['getStatus', 'getLivePrices']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    liveAnnouncerSpy = jasmine.createSpyObj('LiveAnnouncerService', ['announceError', 'announceSuccess']);

    profileServiceSpy.getCachedProfile.and.returnValue({ id: 2, username: 'user', email: 'user@example.com', role: 'USER' });
    profileServiceSpy.getProfile.and.returnValue(of({ id: 2, username: 'user', email: 'user@example.com', role: 'USER' }));
    portfolioServiceSpy.listByUser.and.returnValue(of([
      { id: 2, userId: 2, portfolioName: 'Primary', positions: [{ stockId: 7, quantity: 10, averageBuyPrice: 100 }] }
    ] as Portfolio[]));
    stockServiceSpy.getAllStocks.and.returnValue(of([{ id: 7, stockCode: 'ABC', companyId: 1, stockExchangeId: 1 }] as Stock[]));
    companyServiceSpy.getAllCompanies.and.returnValue(of([{ id: 1, companyName: 'Alpha' }] as Company[]));
    marketLiveServiceSpy.getStatus.and.returnValue(of({ marketOpen: true } as MarketStatus));
    marketLiveServiceSpy.getLivePrices.and.returnValue(of([] as LivePrice[]));

    await TestBed.configureTestingModule({
      imports: [PlaceOrderComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: new Map([['portfolioId', '2'], ['stockId', '7'], ['flow', 'STOP_LOSS_SELL']]) } } },
        { provide: Router, useValue: routerSpy },
        { provide: UserProfileService, useValue: profileServiceSpy },
        { provide: PortfolioService, useValue: portfolioServiceSpy },
        { provide: StockService, useValue: stockServiceSpy },
        { provide: CompanyService, useValue: companyServiceSpy },
        { provide: MarketLiveService, useValue: marketLiveServiceSpy },
        { provide: OrderService, useValue: orderServiceSpy },
        { provide: LiveAnnouncerService, useValue: liveAnnouncerSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PlaceOrderComponent);
    component = fixture.componentInstance;
  });

  it('prefills the selected portfolio from query params and refreshes change detection', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    expect(component.portfolioId).toBe(2);
    expect(component.getSelectedPortfolioName()).toBe('Primary');
    expect(component.stockId).toBe(7);
    expect(component.positions.length).toBe(1);
  }));

  it('maps stop loss to triggerPrice and take profit to orderPrice', () => {
    component.profile = { id: 2, username: 'user', email: 'user@example.com', role: 'USER' };
    component.portfolioId = 2;
    component.stockId = 7;
    component.quantity = 1;
    component.orderFlow = 'TAKE_PROFIT_SELL';
    component.triggerPrice = 180;
    component.positions = [{ stockId: 7, quantity: 3, averageBuyPrice: 100 } as never];
    orderServiceSpy.createOrder.and.returnValue(of({ id: 99, userId: 2, portfolioId: 2, stockId: 7, quantity: 1, idempotencyKey: 'k', orderType: 'TAKE_PROFIT', side: 'SELL', status: 'CREATED', orderPrice: 180, triggerPrice: null, trailAmount: null, referencePrice: null, reservedAmount: null, executedPrice: null, createdAt: '', executedAt: null } as Order));

    component.submitOrder();

    expect(orderServiceSpy.createOrder).toHaveBeenCalled();
    const payload = orderServiceSpy.createOrder.calls.mostRecent().args[0];
    expect(payload.orderPrice).toBe(180);
    expect(payload.triggerPrice).toBeUndefined();
  });

  it('announces submission and delays navigation after a successful order', fakeAsync(() => {
    component.profile = { id: 2, username: 'user', email: 'user@example.com', role: 'USER' };
    component.portfolioId = 2;
    component.stockId = 7;
    component.quantity = 1;
    component.orderFlow = 'MARKET_BUY';
    component.positions = [{ stockId: 7, quantity: 3, averageBuyPrice: 100 } as never];
    orderServiceSpy.createOrder.and.returnValue(of({ id: 101, userId: 2, portfolioId: 2, stockId: 7, quantity: 1, idempotencyKey: 'k', orderType: 'MARKET', side: 'BUY', status: 'CREATED', orderPrice: null, triggerPrice: null, trailAmount: null, referencePrice: null, reservedAmount: null, executedPrice: null, createdAt: '', executedAt: null } as Order));

    component.submitOrder();

    expect(liveAnnouncerSpy.announceStatus).toHaveBeenCalledWith('Submitting order.');
    expect(liveAnnouncerSpy.announceSuccess).toHaveBeenCalledWith('Order #101 created successfully');
    expect(routerSpy.navigate).not.toHaveBeenCalled();

    tick(1499);
    expect(routerSpy.navigate).not.toHaveBeenCalled();

    tick(1);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/investor']);
  }));

  it('shows an error when the portfolio does not own the selected stock for sell flows', () => {
    component.profile = { id: 2, username: 'user', email: 'user@example.com', role: 'USER' };
    component.portfolioId = 2;
    component.stockId = 7;
    component.quantity = 1;
    component.orderFlow = 'STOP_LOSS_SELL';
    component.triggerPrice = 90;
    component.positions = [{ stockId: 8, quantity: 3, averageBuyPrice: 100 } as never];

    component.submitOrder();

    expect(orderServiceSpy.createOrder).not.toHaveBeenCalled();
    expect(component.errorMessage).toContain('Choose a stock you already own');
  });
});
