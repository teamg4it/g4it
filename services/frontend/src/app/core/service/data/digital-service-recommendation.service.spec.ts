import { TestBed } from '@angular/core/testing';

import { DigitalServiceRecommendationService } from './digital-service-recommendation.service';

describe('DigitalServiceRecommendationService', () => {
  let service: DigitalServiceRecommendationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DigitalServiceRecommendationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
