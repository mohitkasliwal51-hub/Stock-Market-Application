import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { ExcelService } from './excel.service';

describe('ExcelService', () => {
  let service: ExcelService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/excel`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ExcelService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(ExcelService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should check excel service health', () => {
    service.health().subscribe(result => {
      expect(result).toBeNull();
    });

    const req = httpMock.expectOne(`${baseUrl}/health`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: null });
  });

  it('should upload excel data', () => {
    const payload = [{ companyId: 1, exchangeId: 1, price: 100, timestamp: '2026-04-06T10:00:00.000+05:30' }] as any;

    service.uploadData(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/uploadData`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    expect(req.request.body).toEqual(payload);
    req.flush({ data: payload });
  });
});
