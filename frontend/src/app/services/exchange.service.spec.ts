/// <reference types="jasmine" />

import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { ExchangeService } from './exchange.service';

describe('ExchangeService', () => {
  let service: ExchangeService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/stockExchanges`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExchangeService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(ExchangeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET all exchanges', () => {
    const mockData = [{ id: 1, name: 'NSE' }] as any;

    service.getAllExchanges().subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should GET exchange by id', () => {
    const mockData = { id: 2, name: 'BSE' } as any;

    service.getExchangeById(2).subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${baseUrl}/2`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should create exchange', () => {
    const payload = { name: 'LSE' } as any;

    service.addExchange(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });

  it('should update exchange', () => {
    const payload = { id: 4, name: 'Updated Exchange' } as any;

    service.updateExchange(4, payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/4`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });

  it('should delete exchange', () => {
    const payload = { id: 5 } as any;

    service.deleteExchange(5).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ data: payload });
  });

  it('should GET companies by exchange', () => {
    const mockCompanies = [{ id: 9, name: 'Company A' }] as any;

    service.getCompaniesByExchange(10).subscribe(result => {
      expect(result).toEqual(mockCompanies);
    });

    const req = httpMock.expectOne(`${baseUrl}/10/companies`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompanies });
  });
});
