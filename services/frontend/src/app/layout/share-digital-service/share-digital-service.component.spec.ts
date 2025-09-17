import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShareDigitalServiceComponent } from './share-digital-service.component';

describe('ShareDigitalServiceComponent', () => {
  let component: ShareDigitalServiceComponent;
  let fixture: ComponentFixture<ShareDigitalServiceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ShareDigitalServiceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ShareDigitalServiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
