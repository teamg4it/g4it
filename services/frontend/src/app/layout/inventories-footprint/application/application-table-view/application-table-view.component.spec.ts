import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationTableViewComponent } from './application-table-view.component';

describe('ApplicationTableViewComponent', () => {
  let component: ApplicationTableViewComponent;
  let fixture: ComponentFixture<ApplicationTableViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationTableViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApplicationTableViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
