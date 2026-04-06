import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { StockService } from './stock.service';

describe('StockService', () => {
  let service: StockService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/stocks`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [StockService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(StockService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET all stocks', () => {
    const mockData = [{ id: 1, stockCode: 'ABC' }] as any;

    service.getAllStocks().subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should create stock', () => {
    const payload = { id: 1, stockCode: 'XYZ' } as any;

    service.addStock(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });
});
