import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeclarationsComponent } from './declarations.component';

describe('DeclarationsComponent', () => {
  let component: DeclarationsComponent;
  let fixture: ComponentFixture<DeclarationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeclarationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DeclarationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
