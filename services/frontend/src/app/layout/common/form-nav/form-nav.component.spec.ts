import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormNavComponent } from './form-nav.component';

describe('FormNavComponent', () => {
  let component: FormNavComponent;
  let fixture: ComponentFixture<FormNavComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormNavComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FormNavComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
