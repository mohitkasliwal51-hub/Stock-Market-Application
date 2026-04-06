import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { StockPriceService } from './stock-price.service';

describe('StockPriceService', () => {
  let service: StockPriceService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/stock-prices`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StockPriceService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(StockPriceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET all stock prices', () => {
    const mockData = [{ id: 1, price: 120 }] as any;

    service.getAllStockPrices().subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should GET stock prices by company and exchange range', () => {
    const from = '2026-04-06T10:00:00.000+05:30';
    const to = '2026-04-06T11:00:00.000+05:30';
    const mockData = [{ id: 2, price: 145 }] as any;

    service.getStockPricesByCompany(2, 3, from, to).subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const expectedUrl = `${baseUrl}/by-company/2/3/${encodeURIComponent(from)}/${encodeURIComponent(to)}`;
    const req = httpMock.expectOne(expectedUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should POST stock prices', () => {
    const payload = [{ id: 1, price: 100 }] as any;

    service.addStockPrices(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });
});
