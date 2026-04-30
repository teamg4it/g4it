/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Recommendation } from './recommendations-data-service';
import { RecommendationService } from './recommendations-data-service';
import { Constants } from 'src/constants';

describe('RecommendationService', () => {
  let service: RecommendationService;
  let httpMock: HttpTestingController;

  // ✅ Simule l'endpoint réel (pas besoin de /evaluation ici)
  
  const mockUrl = 'http://localhost:8080/api/v1'; 

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RecommendationService],
    });

    service = TestBed.inject(RecommendationService);
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
    const workspace = 1;

    const mockResponse: Recommendation[] = [
      {
        idRecommendation: 1,
        title: 'Reco 1',
        description: 'Desc 1',
        category: ['eco'],
        affectedAttributes: '{}',
        heuristicRange: '{}',
        baseImpact: 10,
        organisationId: 123,
        difficulty: 'MEDIUM',
      },
    ];

    service.getByOrganisation(organization, workspace).subscribe((res: Recommendation[]) => {
      expect(res.length).toBe(1);
      expect(res[0].idRecommendation).toBe(1);
    });

    const req = httpMock.expectOne(
      `/organizations/${organization}/workspaces/${workspace}/recommendations`
    );

    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });
  it('should return empty array if backend returns empty', () => {
  const organization = 'org-test';
  const workspace = 2;

  service.getByOrganisation(organization, workspace).subscribe((res: Recommendation[]) => {
    expect(res).toEqual([]);
  });

  const req = httpMock.expectOne(
    `/organizations/${organization}/workspaces/${workspace}/recommendations`
  );

  expect(req.request.method).toBe('GET');
  req.flush([]);
});

  
  it('should handle HTTP error', () => {
    const organization = 'org-test';
    const workspace = 3;

    service.getByOrganisation(organization, workspace).subscribe({
      next: () => fail('should have failed'),
      error: (err: any) => {
        expect(err.status).toBe(500);
      },
    });

    const req = httpMock.expectOne(
      `/organizations/${organization}/workspaces/${workspace}/recommendations`
    );

    expect(req.request.method).toBe('GET');
    req.flush('Error', { status: 500, statusText: 'Server Error' });
  });
});