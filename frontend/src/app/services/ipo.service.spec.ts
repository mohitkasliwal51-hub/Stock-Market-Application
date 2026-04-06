import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { IpoService } from './ipo.service';

describe('IpoService', () => {
  let service: IpoService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/ipos`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [IpoService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(IpoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET all IPOs', () => {
    const mockData = [{ id: 1 }] as any;

    service.getAllIpos().subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should GET IPO by company id', () => {
    const mockData = { id: 2, company: { id: 8 } } as any;

    service.getIpoByCompany(8).subscribe(result => {
      expect(result).toEqual(mockData);
    });

    const req = httpMock.expectOne(`${baseUrl}/by-company/8`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockData });
  });

  it('should create IPO', () => {
    const payload = { id: 3 } as any;

    service.addIpo(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    req.flush({ data: payload });
  });

  it('should update IPO', () => {
    const payload = { id: 4 } as any;

    service.updateIpo(4, payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/4`);
    expect(req.request.method).toBe('PUT');
    req.flush({ data: payload });
  });

  it('should delete IPO', () => {
    const payload = { id: 5 } as any;

    service.deleteIpo(5).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/5`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ data: payload });
  });
});
