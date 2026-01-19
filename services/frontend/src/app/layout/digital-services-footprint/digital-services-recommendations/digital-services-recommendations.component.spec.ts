import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DigitalServicesRecommendationsComponent } from './digital-services-recommendations.component';

describe('DigitalServicesRecommendationsComponent', () => {
  let component: DigitalServicesRecommendationsComponent;
  let fixture: ComponentFixture<DigitalServicesRecommendationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DigitalServicesRecommendationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
