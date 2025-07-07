import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiFileImportComponent } from './multi-file-import.component';

describe('MultiFileImportComponent', () => {
  let component: MultiFileImportComponent;
  let fixture: ComponentFixture<MultiFileImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MultiFileImportComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MultiFileImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
