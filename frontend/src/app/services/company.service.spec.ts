/// <reference types="jasmine" />

import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { CompanyService } from './company.service';

describe('CompanyService', () => {
  let service: CompanyService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/companies`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CompanyService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(CompanyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET all companies', () => {
    const mockCompanies = [{ id: 1, name: 'A' }] as any;

    service.getAllCompanies().subscribe(result => {
      expect(result).toEqual(mockCompanies);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompanies });
  });

  it('should GET company by id', () => {
    const mockCompany = { id: 7, name: 'Test' } as any;

    service.getCompanyById(7).subscribe(result => {
      expect(result).toEqual(mockCompany);
    });

    const req = httpMock.expectOne(`${baseUrl}/7`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompany });
  });

  it('should search company by name pattern', () => {
    const mockCompanies = [{ id: 2, name: 'Alpha' }] as any;

    service.getCompanyByName('alp').subscribe(result => {
      expect(result).toEqual(mockCompanies);
    });

    const req = httpMock.expectOne(`${baseUrl}/search/alp`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompanies });
  });

  it('should create company', () => {
    const payload = { name: 'New Co' } as any;

    service.addCompany(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });

  it('should update company', () => {
    const payload = { id: 3, name: 'Updated Co' } as any;

    service.updateCompany(3, payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/3`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });

  it('should delete company', () => {
    const payload = { id: 4, name: 'Deleted Co' } as any;

    service.deleteCompany(4).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ data: payload });
  });

  it('should GET companies by exchange', () => {
    const mockCompanies = [{ id: 1, name: 'Exchange Co' }] as any;

    service.getCompaniesByExchange(11).subscribe(result => {
      expect(result).toEqual(mockCompanies);
    });

    const req = httpMock.expectOne(`${baseUrl}/by-exchange/11`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompanies });
  });

  it('should GET pending approval companies', () => {
    const mockCompanies = [{ id: 12, name: 'Pending Co' }] as any;

    service.getPendingApprovalCompanies().subscribe(result => {
      expect(result).toEqual(mockCompanies);
    });

    const req = httpMock.expectOne(`${baseUrl}/pending-approval`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompanies });
  });

  it('should update company status', () => {
    const updated = { id: 8, status: 'APPROVED' } as any;

    service.updateCompanyStatus(8, 'approve').subscribe(result => {
      expect(result).toEqual(updated);
    });

    const req = httpMock.expectOne(`${baseUrl}/8/approve`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({});
    req.flush({ data: updated });
  });
});
