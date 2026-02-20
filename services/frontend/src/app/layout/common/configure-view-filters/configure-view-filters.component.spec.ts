import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigureViewFiltersComponent } from './configure-view-filters.component';

describe('ConfigureViewFiltersComponent', () => {
  let component: ConfigureViewFiltersComponent;
  let fixture: ComponentFixture<ConfigureViewFiltersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfigureViewFiltersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConfigureViewFiltersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
