import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphDescriptionComponent } from './graph-description.component';

describe('GraphDescriptionComponent', () => {
  let component: GraphDescriptionComponent;
  let fixture: ComponentFixture<GraphDescriptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GraphDescriptionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GraphDescriptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
