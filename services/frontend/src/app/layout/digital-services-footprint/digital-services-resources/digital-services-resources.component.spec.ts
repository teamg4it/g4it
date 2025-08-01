import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DigitalServicesResourcesComponent } from './digital-services-resources.component';

describe('DigitalServicesResourcesComponent', () => {
  let component: DigitalServicesResourcesComponent;
  let fixture: ComponentFixture<DigitalServicesResourcesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DigitalServicesResourcesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DigitalServicesResourcesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
