import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { SectorService } from './sector.service';

describe('SectorService', () => {
  let service: SectorService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiURL}/api/sectors`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SectorService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(SectorService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should GET all sectors', () => {
    const mockSectors = [{ id: 1, name: 'IT' }] as any;

    service.getAllSectors().subscribe(result => {
      expect(result).toEqual(mockSectors);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockSectors });
  });

  it('should GET sector by id', () => {
    const mockSector = { id: 2, name: 'Finance' } as any;

    service.getSectorById(2).subscribe(result => {
      expect(result).toEqual(mockSector);
    });

    const req = httpMock.expectOne(`${baseUrl}/2`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockSector });
  });

  it('should create sector', () => {
    const payload = { name: 'Energy' } as any;

    service.addSector(payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Content-Type')).toBe('application/json');
    req.flush({ data: payload });
  });

  it('should update sector', () => {
    const payload = { id: 3, name: 'Updated Sector' } as any;

    service.updateSector(3, payload).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/3`);
    expect(req.request.method).toBe('PUT');
    req.flush({ data: payload });
  });

  it('should delete sector', () => {
    const payload = { id: 4 } as any;

    service.deleteSector(4).subscribe(result => {
      expect(result).toEqual(payload);
    });

    const req = httpMock.expectOne(`${baseUrl}/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ data: payload });
  });

  it('should GET companies by sector', () => {
    const mockCompanies = [{ id: 7, name: 'Sector Co' }] as any;

    service.getCompaniesBySector(7).subscribe(result => {
      expect(result).toEqual(mockCompanies);
    });

    const req = httpMock.expectOne(`${baseUrl}/7/companies`);
    expect(req.request.method).toBe('GET');
    req.flush({ data: mockCompanies });
  });
});
