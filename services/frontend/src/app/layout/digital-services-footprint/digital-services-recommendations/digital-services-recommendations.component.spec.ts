import { ComponentFixture, TestBed } from "@angular/core/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { of } from "rxjs";
import { HttpClientTestingModule } from "@angular/common/http/testing";

import { DigitalServicesRecommendationsComponent } from "./digital-services-recommendations.component";
import { UserService } from "src/app/core/service/business/user.service";
import { RecommendationService } from "src/app/core/service/data/recommendations-data-service";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

describe("DigitalServicesRecommendationsComponent", () => {
  let component: DigitalServicesRecommendationsComponent;
  let fixture: ComponentFixture<DigitalServicesRecommendationsComponent>;

  //Mock du UserService : simule une organisation connectée
  const userServiceMock = {
    currentOrganization$: of({ id: 1, name: "Test Org" }),
  };

  // Mock du service API : simule une réponse backend
  const recommendationServiceMock = {
    getByOrganisation: jasmine.createSpy().and.returnValue(
      of([
        {
          idRecommendation: 1,
          title: "Reco 1",
          description: "Desc",
          category: ["NETWORK"],
        },
      ])
    ),
  };

  // Mock du store global (valeurs UI globales)
  const globalStoreMock = {
    zoomLevel: () => 100,
  };

  // Mock du store du service digital
  const digitalServiceStoreMock = {
    digitalService: () => ({ uid: "test-uid" }),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DigitalServicesRecommendationsComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: RecommendationService, useValue: recommendationServiceMock },
        { provide: GlobalStoreService, useValue: globalStoreMock },
        { provide: DigitalServiceStoreService, useValue: digitalServiceStoreMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA], // ignore les composants enfants inconnus
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesRecommendationsComponent);
    component = fixture.componentInstance;

    //Déclenche ngOnInit + subscriptions
    fixture.detectChanges();
  });

  it("should create", () => {
    // Vérifie que le composant est bien instancié
    expect(component).toBeTruthy();
  });

  it("should load recommendations on init", () => {
    //  Vérifie que le service API est appelé avec l'id de l'organisation
    expect(recommendationServiceMock.getByOrganisation).toHaveBeenCalledWith(1);

    //  Vérifie que les données reçues sont bien stockées dans le composant
    expect(component.recommendations.length).toBe(1);
  });

  it("should map category correctly", () => {
    // Test d'une méthode pure (logique métier)
    // Transforme une catégorie backend ("NETWORK")
    // en label affiché ("Réseaux")
    const result = (component as any).mapCategory(["NETWORK"]);

    expect(result).toEqual(["Réseaux"]);
  });

  it("should update selected recommendations", () => {
    // 🔹 On simule une liste avec des éléments sélectionnés ou non
    component.recommendations = [
      { selected: true },
      { selected: false },
    ];

    //  Appel de la logique métier
    component.compareSelected();

    //  Vérifie que seuls les éléments sélectionnés sont gardés
    expect(component.selectedRecommendationsForApply.length).toBe(1);

    //  Vérifie que le composant d'application (UI) doit s'afficher
    expect(component.showApplyComponent).toBeTrue();
  });

  it("should handle select all toggle", () => {
    // Espionne la méthode pour vérifier qu'elle est appelée
    spyOn(component, "compareSelected");

    //  Simule l'action utilisateur (cocher "tout sélectionner")
    component.onSelectAllChange(true);

    // Vérifie que l'état global est bien mis à jour
    expect(component.selectAll).toBeTrue();

    //  Vérifie que la logique de sélection est relancée
    expect(component.compareSelected).toHaveBeenCalled();
  });
});