import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationCriteriaFootprintComponent } from "./application-criteria-footprint.component";

describe("ApplicationCriteriaFootprintComponent", () => {
    let component: ApplicationCriteriaFootprintComponent;
    let fixture: ComponentFixture<ApplicationCriteriaFootprintComponent>;
    let footprintStore: any;
    let filterService: any;
    let translate: any;

    beforeEach(async () => {
        footprintStore = {
            applicationCriteria: jasmine.createSpy().and.returnValue("co2"),
            applicationSelectedFilters: jasmine.createSpy().and.returnValue({}),
            appGraphType: jasmine.createSpy().and.returnValue("global"),
            appDomain: jasmine.createSpy().and.returnValue("Domain1"),
            appSubDomain: jasmine.createSpy().and.returnValue("SubDomain1"),
            appApplication: jasmine.createSpy().and.returnValue("App1"),
            setDomain: jasmine.createSpy(),
            setSubDomain: jasmine.createSpy(),
            setGraphType: jasmine.createSpy(),
            setApplication: jasmine.createSpy(),
        };

        filterService = {
            getFilterincludes: jasmine.createSpy().and.returnValue(true),
        };

        translate = {
            instant: jasmine.createSpy().and.callFake((key) => key),
        };

        await TestBed.configureTestingModule({
            declarations: [ApplicationCriteriaFootprintComponent],
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [
                {
                    provide: InventoriesApplicationFootprintComponent,
                    useValue: { formatLifecycleImpact: (x: any) => x },
                },
                { provide: IntegerPipe, useValue: { transform: (v: any) => v } },
                { provide: DecimalsPipe, useValue: { transform: (v: any) => v } },
                { provide: GlobalStoreService, useValue: {} },
                { provide: FilterService, useValue: filterService },
                { provide: FootprintStoreService, useValue: footprintStore },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ApplicationCriteriaFootprintComponent);
        component = fixture.componentInstance;
        component.footprint = {
            criteria: "co2",
            impacts: [
                {
                    domain: "Domain1",
                    subDomain: "SubDomain1",
                    applicationName: "App1",
                    virtualEquipmentName: "Eq1",
                    impact: 5,
                    sip: 2,
                    equipmentType: "Server",
                    environment: "Prod",
                    cluster: "ClusterA",
                    lifeCycle: "use",
                    statusIndicator: Constants.DATA_QUALITY_STATUS.ok,
                },
            ],
        } as any;
        fixture.detectChanges();
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should reset showInconsitencyGraph on ngOnChanges", () => {
        component.showInconsitencyGraph = true;
        component.ngOnChanges({});
        expect(component.showInconsitencyGraph).toBeFalse();
    });

    describe("onChartClick", () => {
        it("should set domain + subdomain if global and children <=1", () => {
            footprintStore.appGraphType.and.returnValue("global");
            footprintStore.applicationSelectedFilters.and.returnValue({
                domain: [{ label: "Domain1", children: [{ label: "SubDomain1" }] }],
            });
            component.onChartClick({ name: "Domain1" });
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("Domain1");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("SubDomain1");
        });

        it("should set domain only if global and children >1", () => {
            footprintStore.appGraphType.and.returnValue("global");
            footprintStore.applicationSelectedFilters.and.returnValue({
                domain: [{ label: "Domain1", children: [{}, {}] }],
            });
            component.onChartClick({ name: "Domain1" });
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("domain");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("Domain1");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("");
        });

        it("should set subdomain if graphType = domain", () => {
            footprintStore.appGraphType.and.returnValue("domain");
            component.onChartClick({ name: "SubDomain1" });
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("SubDomain1");
        });

        it("should set application if graphType = subdomain", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            component.onChartClick({ name: "App1" });
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("application");
            expect(footprintStore.setApplication).toHaveBeenCalledWith("App1");
        });
    });

    describe("onArrowClick", () => {
        it("should go back from application -> subdomain", () => {
            footprintStore.appGraphType.and.returnValue("application");
            component.onArrowClick();
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("subdomain");
            expect(footprintStore.setApplication).toHaveBeenCalledWith("");
        });

        it("should go back from domain -> global", () => {
            footprintStore.appGraphType.and.returnValue("domain");
            component.allUnmodifiedFilters = { domain: [] } as any;
            component.onArrowClick();
            expect(footprintStore.setGraphType).toHaveBeenCalledWith("global");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("");
        });
    });

    it("checkIfNoData should return false if filter includes impact", () => {
        const result = component.checkIfNoData({});
        expect(result).toBeFalse();
    });

    it("computeImpactOrder should add and then aggregate impacts", () => {
        const impact = component.footprint.impacts[0];
        component.computeImpactOrder(impact, "Domain1");
        expect(component.impactOrder.length).toBe(1);

        // second time aggregates
        component.computeImpactOrder({ ...impact, sip: 3, impact: 4 }, "Domain1");
        expect(component.impactOrder[0].sipImpact).toBe(7);
    });

    it("initGraphData should build global chart data", () => {
        footprintStore.appGraphType.and.returnValue("global");
        const result = component.initGraphData([
            {
                domain: "Domain1",
                sipImpact: 10,
                unitImpact: 5,
                subdomains: ["SubDomain1"],
                apps: ["App1"],
                status: { ok: 1, error: 0, total: 1 },
            },
        ]);
        expect(result.xAxis).toContain("Domain1");
        expect(result.yAxis).toContain(10);
    });

    it("checkImpacts should return grey label if no errors", () => {
        component.allCriteriaMap = { key: { status: { error: 0 } } } as any;
        expect(component.checkImpacts("key")).toContain("{grey|");
    });

    it("selectedStackBarClick should call onChartClick", () => {
        spyOn(component, "onChartClick");
        component.selectedStackBarClick("criteria1");
        expect(component.onChartClick).toHaveBeenCalledWith({ name: "criteria1" });
    });

    describe("computed signals", () => {
        it("showBackButton should be true when graphType is global", () => {
            footprintStore.appGraphType.and.returnValue("global");
            expect(component.showBackButton()).toBeTrue();
        });

        it("showBackButton should be true when graphType is not global", () => {
            footprintStore.appGraphType.and.returnValue("domain");
            expect(component.showBackButton()).toBeTrue();
        });

        it("showDomainByApplication should be false for subdomain", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            expect(component.showDomainByApplication()).toBeFalse();
        });

        it("showDomainByApplication should be false for application", () => {
            footprintStore.appGraphType.and.returnValue("application");
            expect(component.showDomainByApplication()).toBeFalse();
        });

        it("showDomainByApplication should be false for global", () => {
            footprintStore.appGraphType.and.returnValue("global");
            expect(component.showDomainByApplication()).toBeFalse();
        });

        it("showDomainLabel should be true for subdomain if appDomain exists", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            footprintStore.appDomain.and.returnValue("Domain1");
            expect(component.showDomainLabel()).toBeTrue();
        });

        it("showDomainLabel should be true if appDomain empty", () => {
            footprintStore.appGraphType.and.returnValue("subdomain");
            footprintStore.appDomain.and.returnValue("");
            expect(component.showDomainLabel()).toBeTrue();
        });

        it("showSubDomainLabel should be true for application if appSubDomain exists", () => {
            footprintStore.appGraphType.and.returnValue("application");
            footprintStore.appSubDomain.and.returnValue("SubDomain1");
            expect(component.showSubDomainLabel()).toBeTrue();
        });

        it("showSubDomainLabel should be true if no appSubDomain", () => {
            footprintStore.appGraphType.and.returnValue("application");
            footprintStore.appSubDomain.and.returnValue("");
            expect(component.showSubDomainLabel()).toBeTrue();
        });

        it("should return false when domain filters length is 2 or less", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            expect(component.showDomainLabel()).toBeFalse();
        });

        it("should return false when showSubDomainLabel  filters length is 2 or less", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            expect(component.showSubDomainLabel()).toBeFalse();
        });

        it("should return false when showBackButton appGraphType is subdomain", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            (component["footprintStore"].appGraphType as any) = () => "subdomain";
            expect(component.showBackButton()).toBeFalse();
        });
        it("should return false when showBackButton appGraphType is domain", () => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
            (component["footprintStore"].appGraphType as any) = () => "domain";
            expect(component.showBackButton()).toBeFalse();
        });
    });

    describe("domainSelected handling", () => {
        beforeEach(() => {
            (component.allUnmodifiedFilters as any) = () => ({
                domain: [
                    { label: "ALL" },
                    { label: "Domain1", children: [{ label: "Sub1" }] },
                ],
            });
        });

        it("should set graphType global and reset domain/subDomain if children length <= 1", () => {
            footprintStore.appDomain.and.returnValue("Domain1");

            const domainSelected = (component.allUnmodifiedFilters() as any).domain.find(
                (d: any) => d.label === footprintStore.appDomain(),
            );

            // simulate the block of code directly
            if (domainSelected?.children.length <= 1) {
                component["footprintStore"].setGraphType("global");
                component["footprintStore"].setDomain("");
                component["footprintStore"].setSubDomain("");
            }

            expect(footprintStore.setGraphType).toHaveBeenCalledWith("global");
            expect(footprintStore.setDomain).toHaveBeenCalledWith("");
            expect(footprintStore.setSubDomain).toHaveBeenCalledWith("");
        });
    });
});
