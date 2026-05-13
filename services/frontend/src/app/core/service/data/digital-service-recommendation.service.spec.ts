import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { RecommendationDataService } from './digital-service-recommendation.service';

describe('RecommendationDataService', () => {
  let service: RecommendationDataService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });

    service = TestBed.inject(RecommendationDataService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET recommendations with organization and workspace', () => {
    const organization = 'org-test';
    const workspace = 2;

    const mockResponse = [
      {
        idRecommendation: 1,
        title: 'Reco 1',
        description: 'Desc 1',
        category: ['eco'],
        affectedAttributes: '{}',
        heuristicRange: '{}',
        baseImpact: 10,
        organisationId: 123,
        difficulty: 'MEDIUM'
      }
    ];

    service.getRecommendations(organization, workspace).subscribe((res) => {
      expect(res[0].idRecommendation).toBe(1);
      expect(res[0].title).toBe('Reco 1');
    });

    const req = httpMock.expectOne(
      `http://localhost:8080/organizations/${organization}/workspaces/${workspace}/recommendations`
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('should return empty array if backend returns empty', () => {
    const organization = 'org-test';
    const workspace = 2;

    service.getRecommendations(organization, workspace).subscribe((res) => {
      expect(res).toEqual([]);
    });

    const req = httpMock.expectOne(
      `http://localhost:8080/organizations/${organization}/workspaces/${workspace}/recommendations`
    );

    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should handle HTTP error', () => {
    const organization = 'org-test';
    const workspace = 3;

    service.getRecommendations(organization, workspace).subscribe({
      next: () => fail('should have failed'),
      error: (err) => {
        expect(err.status).toBe(500);
      },
    });

    const req = httpMock.expectOne(
      `http://localhost:8080/organizations/${organization}/workspaces/${workspace}/recommendations`
    );

    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });
});