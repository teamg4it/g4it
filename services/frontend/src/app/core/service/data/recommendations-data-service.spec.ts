/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import {
  HttpClientTestingModule,
  HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";

import { Constants } from "src/constants";
import {
  RecommendationService,
  Recommendation} from "./recommendations-data-service"

describe("RecommendationService", () => {
  let service: RecommendationService;
  let httpMock: HttpTestingController;

  const endpoint = Constants.ENDPOINTS.evaluation;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });

    service = TestBed.inject(RecommendationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("should be created", () => {
    expect(service).toBeTruthy();
  });

  it("should get all recommendations", () => {
    const mockResponse: Recommendation[] = [
      {
        idRecommendation: 1,
        title: "Reco 1",
        description: "Desc 1",
        category: ["eco"],
        affectedAttributes: "{}",
        heuristicRange: "{}",
        baseImpact: 10,
        organisationId: 1,
        difficulty: "MEDIUM",
      },
      {
        idRecommendation: 2,
        title: "Reco 2",
        description: "Desc 2",
        category: ["perf"],
        affectedAttributes: "{}",
        heuristicRange: "{}",
        baseImpact: 5,
        organisationId: 1,
        difficulty: "EASY",
      },
    ];

    service.getAllRecommendations().subscribe((res) => {
      expect(res).toHaveSize(2);
      expect(res[0].title).toBe("Reco 1");
      expect(res[1].difficulty).toBe("EASY");
    });

    const req = httpMock.expectOne(`${endpoint}/recommendations`);
    expect(req.request.method).toBe("GET");

    req.flush(mockResponse);
  });

  it("should return empty array if no recommendations", () => {
    service.getAllRecommendations().subscribe((res) => {
      expect(res).toEqual([]);
    });

    const req = httpMock.expectOne(`${endpoint}/recommendations`);
    expect(req.request.method).toBe("GET");

    req.flush([]);
  });

  it("should handle HTTP error", () => {
    let errorResponse: any;

    service.getAllRecommendations().subscribe({
      next: () => fail("should have failed"),
      error: (err) => {
        errorResponse = err;
        expect(err.status).toBe(500);
      },
    });

    const req = httpMock.expectOne(`${endpoint}/recommendations`);
    expect(req.request.method).toBe("GET");

    req.flush("Error", { status: 500, statusText: "Server Error" });
  });
});