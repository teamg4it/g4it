import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateWorkspaceReferenceComponent } from './update-workspace-reference.component';

describe('UpdateWorkspaceReferenceComponent', () => {
  let component: UpdateWorkspaceReferenceComponent;
  let fixture: ComponentFixture<UpdateWorkspaceReferenceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateWorkspaceReferenceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateWorkspaceReferenceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
