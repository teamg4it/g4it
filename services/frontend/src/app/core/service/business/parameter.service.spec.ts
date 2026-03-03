import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { DigitalServiceParameterIa } from "../../interfaces/digital-service.interfaces";
import { ParameterService } from "./parameter.service";

describe("ParameterService", () => {
    let service: ParameterService;
    let httpMock: HttpTestingController;
    const baseUrl = "/api/parameter";

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [ParameterService],
        });
        service = TestBed.inject(ParameterService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    describe("getModels", () => {
        it("should retrieve models from the API", () => {
            const mockModels = ["GPT-4", "BERT", "LLaMA", "Claude"];

            service.getModels().subscribe((models) => {
                expect(models).toEqual(mockModels);
                expect(models.length).toBe(4);
                expect(models[0]).toBe("GPT-4");
            });

            const req = httpMock.expectOne(`${baseUrl}/models`);
            expect(req.request.method).toBe("GET");
            req.flush(mockModels);
        });

        it("should return an empty array when no models are available", () => {
            const mockModels: string[] = [];

            service.getModels().subscribe((models) => {
                expect(models).toEqual([]);
                expect(models.length).toBe(0);
            });

            const req = httpMock.expectOne(`${baseUrl}/models`);
            expect(req.request.method).toBe("GET");
            req.flush(mockModels);
        });

        it("should handle HTTP errors gracefully", () => {
            const errorMessage = "Server error";

            service.getModels().subscribe(
                () => fail("should have failed with 500 error"),
                (error) => {
                    expect(error.status).toBe(500);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/models`);
            expect(req.request.method).toBe("GET");
            req.flush(errorMessage, { status: 500, statusText: "Server Error" });
        });

        it("should handle network errors", () => {
            service.getModels().subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/models`);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should return multiple models", () => {
            const mockModels = [
                "GPT-3.5",
                "GPT-4",
                "BERT-base",
                "BERT-large",
                "LLaMA-7B",
                "LLaMA-13B",
            ];

            service.getModels().subscribe((models) => {
                expect(models.length).toBe(6);
                expect(models).toContain("GPT-4");
                expect(models).toContain("BERT-base");
            });

            const req = httpMock.expectOne(`${baseUrl}/models`);
            req.flush(mockModels);
        });
    });

    describe("getParameters", () => {
        it("should retrieve parameters from the API", () => {
            const mockParameters = ["7B", "13B", "70B", "175B"];

            service.getParameters().subscribe((parameters) => {
                expect(parameters).toEqual(mockParameters);
                expect(parameters.length).toBe(4);
                expect(parameters[0]).toBe("7B");
            });

            const req = httpMock.expectOne(`${baseUrl}/parameters`);
            expect(req.request.method).toBe("GET");
            req.flush(mockParameters);
        });

        it("should return an empty array when no parameters are available", () => {
            const mockParameters: string[] = [];

            service.getParameters().subscribe((parameters) => {
                expect(parameters).toEqual([]);
                expect(parameters.length).toBe(0);
            });

            const req = httpMock.expectOne(`${baseUrl}/parameters`);
            expect(req.request.method).toBe("GET");
            req.flush(mockParameters);
        });

        it("should handle HTTP errors gracefully", () => {
            const errorMessage = "Not found";

            service.getParameters().subscribe(
                () => fail("should have failed with 404 error"),
                (error) => {
                    expect(error.status).toBe(404);
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/parameters`);
            req.flush(errorMessage, { status: 404, statusText: "Not Found" });
        });

        it("should retrieve numeric parameter values", () => {
            const mockParameters = ["1B", "3B", "7B", "13B", "33B", "65B"];

            service.getParameters().subscribe((parameters) => {
                expect(parameters.length).toBe(6);
                expect(parameters).toContain("7B");
                expect(parameters).toContain("65B");
            });

            const req = httpMock.expectOne(`${baseUrl}/parameters`);
            req.flush(mockParameters);
        });
    });

    describe("getFrameworks", () => {
        it("should retrieve frameworks from the API", () => {
            const mockFrameworks = ["PyTorch", "TensorFlow", "JAX", "Hugging Face"];

            service.getFrameworks().subscribe((frameworks) => {
                expect(frameworks).toEqual(mockFrameworks);
                expect(frameworks.length).toBe(4);
                expect(frameworks[0]).toBe("PyTorch");
            });

            const req = httpMock.expectOne(`${baseUrl}/frameworks`);
            expect(req.request.method).toBe("GET");
            req.flush(mockFrameworks);
        });

        it("should return an empty array when no frameworks are available", () => {
            const mockFrameworks: string[] = [];

            service.getFrameworks().subscribe((frameworks) => {
                expect(frameworks).toEqual([]);
                expect(frameworks.length).toBe(0);
            });

            const req = httpMock.expectOne(`${baseUrl}/frameworks`);
            expect(req.request.method).toBe("GET");
            req.flush(mockFrameworks);
        });

        it("should handle HTTP errors gracefully", () => {
            const errorMessage = "Unauthorized";

            service.getFrameworks().subscribe(
                () => fail("should have failed with 401 error"),
                (error) => {
                    expect(error.status).toBe(401);
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/frameworks`);
            req.flush(errorMessage, { status: 401, statusText: "Unauthorized" });
        });

        it("should retrieve multiple ML frameworks", () => {
            const mockFrameworks = [
                "PyTorch",
                "TensorFlow",
                "Keras",
                "Scikit-learn",
                "XGBoost",
            ];

            service.getFrameworks().subscribe((frameworks) => {
                expect(frameworks.length).toBe(5);
                expect(frameworks).toContain("PyTorch");
                expect(frameworks).toContain("Keras");
            });

            const req = httpMock.expectOne(`${baseUrl}/frameworks`);
            req.flush(mockFrameworks);
        });
    });

    describe("getQuantizations", () => {
        it("should retrieve quantizations from the API", () => {
            const mockQuantizations = ["FP32", "FP16", "INT8", "INT4"];

            service.getQuantizations().subscribe((quantizations) => {
                expect(quantizations).toEqual(mockQuantizations);
                expect(quantizations.length).toBe(4);
                expect(quantizations[0]).toBe("FP32");
            });

            const req = httpMock.expectOne(`${baseUrl}/quantizations`);
            expect(req.request.method).toBe("GET");
            req.flush(mockQuantizations);
        });

        it("should return an empty array when no quantizations are available", () => {
            const mockQuantizations: string[] = [];

            service.getQuantizations().subscribe((quantizations) => {
                expect(quantizations).toEqual([]);
                expect(quantizations.length).toBe(0);
            });

            const req = httpMock.expectOne(`${baseUrl}/quantizations`);
            expect(req.request.method).toBe("GET");
            req.flush(mockQuantizations);
        });

        it("should handle HTTP errors gracefully", () => {
            const errorMessage = "Service unavailable";

            service.getQuantizations().subscribe(
                () => fail("should have failed with 503 error"),
                (error) => {
                    expect(error.status).toBe(503);
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/quantizations`);
            req.flush(errorMessage, {
                status: 503,
                statusText: "Service Unavailable",
            });
        });

        it("should retrieve various quantization formats", () => {
            const mockQuantizations = ["FP32", "FP16", "BF16", "INT8", "INT4", "GPTQ"];

            service.getQuantizations().subscribe((quantizations) => {
                expect(quantizations.length).toBe(6);
                expect(quantizations).toContain("INT8");
                expect(quantizations).toContain("GPTQ");
            });

            const req = httpMock.expectOne(`${baseUrl}/quantizations`);
            req.flush(mockQuantizations);
        });
    });

    describe("submitForm", () => {
        const mockFormData: DigitalServiceParameterIa = {
            id: 1,
            modelName: "GPT-4",
            nbParameters: "175B",
            framework: "PyTorch",
            quantization: "FP16",
            totalGeneratedTokens: 1000000,
            numberUserYear: 10000,
            averageNumberRequest: 100,
            averageNumberToken: 500,
            isInference: true,
            isFinetuning: false,
            creationDate: "2024-01-01",
            lastUpdateDate: "2024-01-15",
        };

        it("should submit form data to the API", () => {
            const mockResponse = { success: true, id: 1 };

            service.submitForm(mockFormData).subscribe((response) => {
                expect(response).toEqual(mockResponse);
                expect(response.success).toBe(true);
                expect(response.id).toBe(1);
            });

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.method).toBe("POST");
            expect(req.request.body).toEqual(mockFormData);
            req.flush(mockResponse);
        });

        it("should send correct request body", () => {
            service.submitForm(mockFormData).subscribe();

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.body.modelName).toBe("GPT-4");
            expect(req.request.body.nbParameters).toBe("175B");
            expect(req.request.body.framework).toBe("PyTorch");
            expect(req.request.body.quantization).toBe("FP16");
            expect(req.request.body.isInference).toBe(true);
            expect(req.request.body.isFinetuning).toBe(false);
            req.flush({});
        });

        it("should handle successful submission with response data", () => {
            const mockResponse = {
                success: true,
                id: 123,
                message: "Data submitted successfully",
            };

            service.submitForm(mockFormData).subscribe((response) => {
                expect(response.success).toBe(true);
                expect(response.id).toBe(123);
                expect(response.message).toBe("Data submitted successfully");
            });

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            req.flush(mockResponse);
        });

        it("should handle HTTP errors gracefully", () => {
            const errorMessage = "Validation error";

            service.submitForm(mockFormData).subscribe(
                () => fail("should have failed with 400 error"),
                (error) => {
                    expect(error.status).toBe(400);
                    expect(error.error).toBe(errorMessage);
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            req.flush(errorMessage, { status: 400, statusText: "Bad Request" });
        });

        it("should handle network errors", () => {
            service.submitForm(mockFormData).subscribe(
                () => fail("should have failed with network error"),
                (error) => {
                    expect(error.error.type).toBe("error");
                },
            );

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            req.error(new ErrorEvent("error", { message: "Network error" }));
        });

        it("should submit form with finetuning enabled", () => {
            const finetuningData = {
                ...mockFormData,
                isInference: false,
                isFinetuning: true,
            };

            service.submitForm(finetuningData).subscribe();

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.body.isInference).toBe(false);
            expect(req.request.body.isFinetuning).toBe(true);
            req.flush({ success: true });
        });

        it("should submit form with both inference and finetuning", () => {
            const combinedData = {
                ...mockFormData,
                isInference: true,
                isFinetuning: true,
            };

            service.submitForm(combinedData).subscribe();

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.body.isInference).toBe(true);
            expect(req.request.body.isFinetuning).toBe(true);
            req.flush({ success: true });
        });

        it("should submit form with large token counts", () => {
            const largeTokenData = {
                ...mockFormData,
                totalGeneratedTokens: 1000000000,
                numberUserYear: 500000,
                averageNumberRequest: 10000,
                averageNumberToken: 5000,
            };

            service.submitForm(largeTokenData).subscribe();

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.body.totalGeneratedTokens).toBe(1000000000);
            expect(req.request.body.numberUserYear).toBe(500000);
            req.flush({ success: true });
        });

        it("should submit form with updated dates", () => {
            const dataWithDates = {
                ...mockFormData,
                creationDate: "2024-03-01",
                lastUpdateDate: "2024-03-03",
            };

            service.submitForm(dataWithDates).subscribe();

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.body.creationDate).toBe("2024-03-01");
            expect(req.request.body.lastUpdateDate).toBe("2024-03-03");
            req.flush({ success: true });
        });

        it("should handle empty response", () => {
            service.submitForm(mockFormData).subscribe((response) => {
                expect(response).toEqual({});
            });

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            req.flush({});
        });
    });

    describe("Multiple API calls", () => {
        it("should handle multiple concurrent GET requests", () => {
            const mockModels = ["GPT-4", "BERT"];
            const mockParameters = ["7B", "13B"];
            const mockFrameworks = ["PyTorch", "TensorFlow"];
            const mockQuantizations = ["FP16", "INT8"];

            service.getModels().subscribe((models) => {
                expect(models).toEqual(mockModels);
            });
            service.getParameters().subscribe((parameters) => {
                expect(parameters).toEqual(mockParameters);
            });
            service.getFrameworks().subscribe((frameworks) => {
                expect(frameworks).toEqual(mockFrameworks);
            });
            service.getQuantizations().subscribe((quantizations) => {
                expect(quantizations).toEqual(mockQuantizations);
            });

            const reqModels = httpMock.expectOne(`${baseUrl}/models`);
            const reqParameters = httpMock.expectOne(`${baseUrl}/parameters`);
            const reqFrameworks = httpMock.expectOne(`${baseUrl}/frameworks`);
            const reqQuantizations = httpMock.expectOne(`${baseUrl}/quantizations`);

            reqModels.flush(mockModels);
            reqParameters.flush(mockParameters);
            reqFrameworks.flush(mockFrameworks);
            reqQuantizations.flush(mockQuantizations);
        });

        it("should handle sequential API calls", () => {
            const mockModels = ["GPT-4"];
            const mockResponse = { success: true };

            let modelsReceived = false;

            service.getModels().subscribe((models) => {
                expect(models).toEqual(mockModels);
                modelsReceived = true;
            });

            const reqModels = httpMock.expectOne(`${baseUrl}/models`);
            reqModels.flush(mockModels);

            expect(modelsReceived).toBe(true);

            const formData: DigitalServiceParameterIa = {
                id: 1,
                modelName: "GPT-4",
                nbParameters: "175B",
                framework: "PyTorch",
                quantization: "FP16",
                totalGeneratedTokens: 1000,
                numberUserYear: 100,
                averageNumberRequest: 10,
                averageNumberToken: 50,
                isInference: true,
                isFinetuning: false,
                creationDate: "2024-01-01",
                lastUpdateDate: "2024-01-01",
            };

            service.submitForm(formData).subscribe((response) => {
                expect(response).toEqual(mockResponse);
            });

            const reqSubmit = httpMock.expectOne(`${baseUrl}/submit`);
            reqSubmit.flush(mockResponse);
        });
    });

    describe("baseUrl configuration", () => {
        it("should use correct base URL for all endpoints", () => {
            service.getModels().subscribe();
            service.getParameters().subscribe();
            service.getFrameworks().subscribe();
            service.getQuantizations().subscribe();

            const reqModels = httpMock.expectOne(`${baseUrl}/models`);
            const reqParameters = httpMock.expectOne(`${baseUrl}/parameters`);
            const reqFrameworks = httpMock.expectOne(`${baseUrl}/frameworks`);
            const reqQuantizations = httpMock.expectOne(`${baseUrl}/quantizations`);

            expect(reqModels.request.url).toBe("/api/parameter/models");
            expect(reqParameters.request.url).toBe("/api/parameter/parameters");
            expect(reqFrameworks.request.url).toBe("/api/parameter/frameworks");
            expect(reqQuantizations.request.url).toBe("/api/parameter/quantizations");

            reqModels.flush([]);
            reqParameters.flush([]);
            reqFrameworks.flush([]);
            reqQuantizations.flush([]);
        });

        it("should use correct base URL for submit endpoint", () => {
            const formData: DigitalServiceParameterIa = {
                id: 1,
                modelName: "Test",
                nbParameters: "1B",
                framework: "Test",
                quantization: "FP32",
                totalGeneratedTokens: 100,
                numberUserYear: 10,
                averageNumberRequest: 1,
                averageNumberToken: 10,
                isInference: true,
                isFinetuning: false,
                creationDate: "2024-01-01",
                lastUpdateDate: "2024-01-01",
            };

            service.submitForm(formData).subscribe();

            const req = httpMock.expectOne(`${baseUrl}/submit`);
            expect(req.request.url).toBe("/api/parameter/submit");
            req.flush({});
        });
    });
});
