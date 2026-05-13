import { ComponentFixture, TestBed } from "@angular/core/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { of, throwError } from "rxjs";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { fakeAsync, tick } from "@angular/core/testing";

import { DigitalServicesRecommendationsComponent } from "./digital-services-recommendations.component";
import { UserService } from "src/app/core/service/business/user.service";
import { RecommendationService } from "src/app/core/service/data/recommendations-data-service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

describe("DigitalServicesRecommendationsComponent", () => {
  let component: DigitalServicesRecommendationsComponent;
  let fixture: ComponentFixture<DigitalServicesRecommendationsComponent>;

  const recommendationServiceMock = {
    getByOrganisation: jasmine.createSpy().and.returnValue(of([])),

    getInstantiatedRecommendations: jasmine.createSpy().and.returnValue(
      of([
        {
          idInstantiatedRecommendation: 1,
          priority: 1,
          recommendation: {
            idRecommendation: 1,
            title: "Reco 1",
            description: "Desc",
            category: ["NETWORK"],
            difficulty: "HARD",
          },
        },
      ])
    ),
  };

  const globalStoreMock = {
    zoomLevel: () => 100,
  };

  const digitalServiceStoreMock = {
    digitalService: () => ({ uid: "test-uid" }),
  };

  let userServiceMock: any;

  beforeEach(async () => {
    userServiceMock = {
      currentOrganization$: of({
        id: 1,
        name: "Test Org",
        workspaces: [{ id: 1 }],
      }),
    };

    await TestBed.configureTestingModule({
      declarations: [DigitalServicesRecommendationsComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: RecommendationService, useValue: recommendationServiceMock },
        { provide: GlobalStoreService, useValue: globalStoreMock },
        { provide: DigitalServiceStoreService, useValue: digitalServiceStoreMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    (component as any).digitalService$ = of({ uid: "test-uid" });

    fixture.detectChanges();
  });

  // =========================
  // BASIC
  // =========================

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  // =========================
  // MAPPING
  // =========================

  it("should map category correctly", () => {
    const result = (component as any).mapCategory(["NETWORK"]);
    expect(result).toEqual(["categories.NETWORK"]);
  });

  it("should return original category if unknown", () => {
    const result = (component as any).mapCategory(["OTHER"]);
    expect(result).toEqual(["OTHER"]);
  });

  // =========================
  // SELECTION LOGIC
  // =========================

  it("should update selected recommendations", () => {
    component.recommendations = [
      { selected: true },
      { selected: false },
    ];

    component.compareSelected();

    expect(component.selectedRecommendationsForApply.length).toBe(1);
    expect(component.showApplyComponent).toBeTrue();
  });

  it("should handle select all toggle", () => {
    spyOn(component, "compareSelected");

    component.onSelectAllChange(true);

    expect(component.selectAll).toBeTrue();
    expect(component.compareSelected).toHaveBeenCalled();
  });

  // =========================
  // API LOADING
  // =========================

  it("should load instantiated recommendations on init", fakeAsync(() => {
    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    (component as any).digitalService$ = of({ uid: "test-uid" });

    fixture.detectChanges();
    tick();

    expect(
      recommendationServiceMock.getInstantiatedRecommendations
    ).toHaveBeenCalledWith("Test Org", 1, "test-uid");

    expect(component.recommendations.length).toBe(1);
  }));

  it("should not call API if organization is invalid", () => {
    recommendationServiceMock.getInstantiatedRecommendations.calls.reset();

    userServiceMock.currentOrganization$ = of(null as any);

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    (component as any).digitalService$ = of({ uid: "test-uid" });

    fixture.detectChanges();

    expect(
      recommendationServiceMock.getInstantiatedRecommendations
    ).not.toHaveBeenCalled();
  });

  // =========================
  // DIFFICULTY
  // =========================

  it("should set difficulty to null if missing", () => {
    recommendationServiceMock.getInstantiatedRecommendations.and.returnValue(
      of([
        {
          priority: 1,
          recommendation: {
            category: ["NETWORK"],
            difficulty: null,
          },
        },
      ])
    );

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    (component as any).digitalService$ = of({ uid: "test-uid" });

    fixture.detectChanges();

    expect(component.recommendations[0].implementationDifficulty).toBeNull();
  });

  it("should keep difficulty formatted as prefix", () => {
    recommendationServiceMock.getInstantiatedRecommendations.and.returnValue(
      of([
        {
          priority: 1,
          recommendation: {
            category: ["NETWORK"],
            difficulty: "HARD",
          },
        },
      ])
    );

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    (component as any).digitalService$ = of({ uid: "test-uid" });

    fixture.detectChanges();

    expect(component.recommendations[0].implementationDifficulty).toBe(
      "difficulty.HARD"
    );
  });

  

  // =========================
  // ZOOM
  // =========================

  it("should return true when zoom >= 125", () => {
    globalStoreMock.zoomLevel = () => 130;

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    expect(component.isZoom125()).toBeTrue();
  });

  it("should return false when zoom < 125", () => {
    globalStoreMock.zoomLevel = () => 100;

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    expect(component.isZoom125()).toBeFalse();
  });
});