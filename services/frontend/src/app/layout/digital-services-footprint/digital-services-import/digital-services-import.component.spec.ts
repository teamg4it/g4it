import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DigitalServicesImportComponent } from './digital-services-import.component';

describe('DigitalServicesImportComponent', () => {
  let component: DigitalServicesImportComponent;
  let fixture: ComponentFixture<DigitalServicesImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DigitalServicesImportComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DigitalServicesImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
