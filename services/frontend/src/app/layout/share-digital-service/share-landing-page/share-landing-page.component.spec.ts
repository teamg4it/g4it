import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShareLandingPageComponent } from './share-landing-page.component';

describe('ShareLandingPageComponent', () => {
  let component: ShareLandingPageComponent;
  let fixture: ComponentFixture<ShareLandingPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShareLandingPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShareLandingPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
