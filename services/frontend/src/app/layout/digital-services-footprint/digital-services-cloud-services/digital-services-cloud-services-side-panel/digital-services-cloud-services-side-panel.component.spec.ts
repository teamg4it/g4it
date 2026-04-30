import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { of } from "rxjs";
import { Pipe, PipeTransform } from "@angular/core";

import { DigitalServicesCloudServicesSidePanelComponent } from "./digital-services-cloud-services-side-panel.component";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";

/* ---------------- MOCK PIPE TRANSLATE ---------------- */
@Pipe({ name: "translate" })
class MockTranslatePipe implements PipeTransform {
  transform(value: any): any {
    return value;
  }
}

describe("DigitalServicesCloudServicesSidePanelComponent", () => {
  let component: DigitalServicesCloudServicesSidePanelComponent;
  let fixture: ComponentFixture<DigitalServicesCloudServicesSidePanelComponent>;

  const storeMock = {
    countryMap: () => ({
      FR: "France",
      DE: "Germany",
    }),
  };

  const dataServiceMock = {
    getBoaviztapiCloudProviders: jasmine
      .createSpy()
      .and.returnValue(of(["aws", "gcp"])),

    getBoaviztapiInstanceTypes: jasmine
      .createSpy()
      .and.returnValue(of(["small", "large"])),
  };

  const businessMock = {
    getNextAvailableName: jasmine.createSpy().and.returnValue("Cloud Service 1"),
  };

  const routerMock = {
    url: "/digital-service/xxx/xxx/xxx/xxx/xxx/123",
  };

  const userServiceMock = {};

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [
        DigitalServicesCloudServicesSidePanelComponent,
        MockTranslatePipe,
      ],
      providers: [
        { provide: DigitalServiceStoreService, useValue: storeMock },
        { provide: DigitalServicesDataService, useValue: dataServiceMock },
        { provide: DigitalServiceBusinessService, useValue: businessMock },
        { provide: Router, useValue: routerMock },
        { provide: UserService, useValue: userServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DigitalServicesCloudServicesSidePanelComponent);
    component = fixture.componentInstance;

    component.cloudServices = [];
    component.cloud = {
      idFront: undefined,
    } as any;

    fixture.detectChanges();
  });

  /* ---------------- CREATION ---------------- */
  it("should create", () => {
    expect(component).toBeTruthy();
  });

  /* ---------------- INIT ---------------- */
  it("should initialize form on init", async () => {
    await component.ngOnInit();

    expect(component.cloudForm).toBeDefined();
    expect(component.cloudForm.get("name")).toBeTruthy();
  });

  it("should load providers and instance types", async () => {
    await component.getBoaviztaReferentials();

    expect(dataServiceMock.getBoaviztapiCloudProviders).toHaveBeenCalled();
    expect(dataServiceMock.getBoaviztapiInstanceTypes).toHaveBeenCalled();
    expect(component.cloudProviders.length).toBe(2);
  });

  /* ---------------- RESET ---------------- */
  it("should reset cloud services", async () => {
    await component.resetCloudServices();

    expect(component.cloud.name).toContain("Cloud Service");
    expect(component.cloud.quantity).toBe(1);
    expect(component.cloud.location.name).toBe("Europe");
  });

  /* ---------------- FORM ---------------- */
  it("should init form with validators", () => {
    component.cloudServices = [{ name: "Existing" } as any];

    component.initForm();

    expect(component.cloudForm).toBeDefined();
    expect(component.cloudForm.get("name")).toBeTruthy();
  });

  /* ---------------- CLOSE ---------------- */
  it("should emit close event", () => {
    spyOn(component.sidebarVisibleChange, "emit");

    component.close();

    expect(component.sidebarVisibleChange.emit).toHaveBeenCalledWith(false);
  });

  /* ---------------- SUBMIT ---------------- */
  it("should emit updateCloudServices and close when not embedded", async () => {
    spyOn(component.updateCloudServices, "emit");
    spyOn(component, "close");

    component.embedded = false;

    await component.submitFormData();

    expect(component.updateCloudServices.emit).toHaveBeenCalled();
    expect(component.close).toHaveBeenCalled();
  });

  it("should emit updateCloudServices but NOT close when embedded", async () => {
    spyOn(component.updateCloudServices, "emit");
    spyOn(component, "close");

    component.embedded = true;

    await component.submitFormData();

    expect(component.updateCloudServices.emit).toHaveBeenCalled();
    expect(component.close).not.toHaveBeenCalled();
  });

  /* ---------------- DELETE ---------------- */
  it("should emit deleteCloudServices and close when not embedded", async () => {
    spyOn(component.deleteCloudServices, "emit");
    spyOn(component, "close");

    component.embedded = false;

    await component.deleteServerCloud();

    expect(component.deleteCloudServices.emit).toHaveBeenCalled();
    expect(component.close).toHaveBeenCalled();
  });

  it("should emit deleteCloudServices but NOT close when embedded", async () => {
    spyOn(component.deleteCloudServices, "emit");
    spyOn(component, "close");

    component.embedded = true;

    await component.deleteServerCloud();

    expect(component.deleteCloudServices.emit).toHaveBeenCalled();
    expect(component.close).not.toHaveBeenCalled();
  });

  /* ---------------- EMBEDDED FLAG ---------------- */
  it("should respect embedded flag", () => {
    component.embedded = true;
    expect(component.embedded).toBeTrue();
  });
});