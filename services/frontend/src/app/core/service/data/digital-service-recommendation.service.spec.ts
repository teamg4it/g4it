import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { RecommendationDataService } from './digital-service-recommendation.service';

describe('RecommendationDataService', () => {
  let service: RecommendationDataService;
  let httpMock: HttpTestingController;

  const mockUrl = 'http://localhost:8080/api/v1/evaluation';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });

    service = TestBed.inject(RecommendationDataService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // vérifie qu’il n’y a pas de requêtes non traitées
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call GET /recommendations with organisationId', () => {
  const organisationId = 123;

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

  service.getRecommendations(organisationId).subscribe((res) => {
    expect(res[0].idRecommendation).toBe(1);
    expect(res[0].title).toBe('Reco 1');
  });

  const req = httpMock.expectOne(
    `http://localhost:8080/api/v1/evaluation/organisations/${organisationId}/recommendations`
  );

  expect(req.request.method).toBe('GET');

  req.flush(mockResponse);
});

  it('should return empty array if backend returns empty', () => {
    const organisationId = 456;

    service.getRecommendations(organisationId).subscribe((res) => {
      expect(res).toEqual([]);
    });

    const req = httpMock.expectOne(
      `${mockUrl}/organisations/${organisationId}/recommendations`
    );

    expect(req.request.method).toBe('GET');

    req.flush([]);
  });

  it('should handle HTTP error', () => {
    const organisationId = 789;

    let errorResponse: any;

    service.getRecommendations(organisationId).subscribe({
      next: () => fail('should have failed'),
      error: (err) => {
        errorResponse = err;
        expect(err.status).toBe(500);
      },
    });

    const req = httpMock.expectOne(
      `${mockUrl}/organisations/${organisationId}/recommendations`
    );

    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });
});